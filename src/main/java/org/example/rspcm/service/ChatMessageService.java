package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.ChatMessageRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Chat;
import org.example.rspcm.model.entity.ChatMessage;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.ChatRepository;
import org.example.rspcm.repository.ChatMessageRepository;
import org.example.rspcm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendMessage(Long chatId, ChatMessageRequest request, String name) {
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
        repository.save(message);
    }
}
