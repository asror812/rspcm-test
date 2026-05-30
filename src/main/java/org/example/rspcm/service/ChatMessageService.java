package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.ChatMessageRequest;
import org.example.rspcm.dto.chat.ChatMessageResponse;
import org.example.rspcm.dto.chat.ChatSummaryResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Chat;
import org.example.rspcm.model.entity.ChatMessage;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.ChatRepository;
import org.example.rspcm.repository.ChatMessageRepository;
import org.example.rspcm.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(Long chatId, ChatMessageRequest request, String name) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ErrorMessageException("Сообщение не может быть пустым", ErrorCodes.BadRequest);
        }

        User sender = userRepository.findByEmailAndEnabledTrueAndDeletedFalse(name)
                .or(() -> userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(name))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + name));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Чат не найден: " + chatId));

        boolean isMember = chatRepository.existsByIdAndMemberIdentifier(chatId, name);
        if (!isMember) {
            throw new ErrorMessageException("Нет доступа к чату", ErrorCodes.Forbidden);
        }

        ChatMessage message = new ChatMessage();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getMessage().trim());
        ChatMessage saved = repository.save(message);
        ChatMessageResponse response = toMessageResponse(saved, name);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/messages", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> getMyChats(String name) {
        return chatRepository.findAllByMemberIdentifier(name).stream()
                .map(chat -> {
                    ChatSummaryResponse response = new ChatSummaryResponse();
                    response.setId(chat.getId());
                    response.setTitle(chat.getTitle());
                    response.setType(chat.getType().name());
                    response.setLastMessage(repository.findTopByChatIdOrderByIdDesc(chat.getId())
                            .map(ChatMessage::getContent)
                            .orElse(""));
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(Long chatId, String name) {
        ensureChatMembership(chatId, name);
        return repository.findAllByChatIdOrderByIdAsc(chatId).stream()
                .map(message -> toMessageResponse(message, name))
                .toList();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message, String name) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setChatId(message.getChat().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        response.setContent(message.getContent());
        response.setMine(name.equals(message.getSender().getEmail()) || name.equals(message.getSender().getUniversityId()));
        return response;
    }

    private void ensureChatMembership(Long chatId, String name) {
        chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Чат не найден: " + chatId));

        if (!chatRepository.existsByIdAndMemberIdentifier(chatId, name)) {
            throw new ErrorMessageException("Нет доступа к чату", ErrorCodes.Forbidden);
        }
    }
}
