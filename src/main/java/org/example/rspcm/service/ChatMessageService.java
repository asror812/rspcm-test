package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.ChatMessageRequest;
import org.example.rspcm.dto.chat.ChatAttachmentResponse;
import org.example.rspcm.dto.chat.ChatMessageResponse;
import org.example.rspcm.dto.chat.ChatSummaryResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Chat;
import org.example.rspcm.model.entity.ChatAttachment;
import org.example.rspcm.model.entity.ChatMember;
import org.example.rspcm.model.entity.ChatMessage;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.ChatMemberRepository;
import org.example.rspcm.repository.ChatRepository;
import org.example.rspcm.repository.ChatMessageRepository;
import org.example.rspcm.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.rspcm.dto.chat.ChatMemberResponse;
import org.example.rspcm.model.enums.ChatMemberRole;
import org.example.rspcm.model.enums.ChatType;
import org.example.rspcm.repository.StudyGroupRepository;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatPresenceService chatPresenceService;
    private final FileStorageService fileStorageService;
    private final MessageService messageService;
    private final StudyGroupRepository studyGroupRepository;

    @Transactional
    public ChatMessageResponse sendMessage(Long chatId, ChatMessageRequest request, String name) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ErrorMessageException(messageService.get("error.message.empty"), ErrorCodes.BadRequest);
        }

        User sender = userRepository.findByEmailAndEnabledTrueAndDeletedFalse(name)
                .or(() -> userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(name))
                .orElseThrow(() -> new NotFoundException("User not found: " + name));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

        boolean isMember = chatRepository.existsByIdAndMemberIdentifier(chatId, name);
        if (!isMember) {
            throw new ErrorMessageException(messageService.get("error.chat.no.access"), ErrorCodes.Forbidden);
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

    @Transactional
    public ChatMessageResponse sendMessageWithAttachment(Long chatId, String content, MultipartFile file, String name) {
        String text = content == null ? null : content.trim();
        if ((text == null || text.isEmpty()) && (file == null || file.isEmpty())) {
            throw new ErrorMessageException(messageService.get("error.message.or.attachment.required"), ErrorCodes.BadRequest);
        }

        User sender = userRepository.findByEmailAndEnabledTrueAndDeletedFalse(name)
                .or(() -> userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(name))
                .orElseThrow(() -> new NotFoundException("User not found: " + name));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));
        ensureChatMembership(chatId, name);

        ChatMessage message = new ChatMessage();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(text);

        if (file != null && !file.isEmpty()) {
            FileStorageService.StoredFile storedFile = fileStorageService.storeChatFile(file);
            ChatAttachment attachment = new ChatAttachment();
            attachment.setMessage(message);
            attachment.setFileName(storedFile.getFileName());
            attachment.setStoredName(storedFile.getStoredName());
            attachment.setContentType(storedFile.getContentType());
            attachment.setSize(storedFile.getSize());
            attachment.setFilePath(storedFile.getAbsolutePath());
            message.getAttachments().add(attachment);
        }

        ChatMessage saved = repository.save(message);
        ChatMessageResponse response = toMessageResponse(saved, name);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/messages", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> getMyChats(String name) {
        List<Chat> chats = chatRepository.findAllByMemberIdentifier(name);
        Set<Long> chatIds = chats.stream().map(Chat::getId).collect(Collectors.toSet());

        Map<Long, Long> memberCounts = chatMemberRepository.countMembersByChatIds(chatIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        Map<Long, Integer> onlineCounts = chatPresenceService.onlineCounts(chatIds);

        return chats.stream()
                .map(chat -> {
                    ChatSummaryResponse response = new ChatSummaryResponse();
                    response.setId(chat.getId());
                    response.setTitle(chat.getTitle());
                    response.setType(chat.getType().name());
                    response.setLastMessage(repository.findTopByChatIdOrderByIdDesc(chat.getId())
                            .map(ChatMessage::getContent)
                            .orElse(""));
                    response.setMemberCount(memberCounts.getOrDefault(chat.getId(), 0L));
                    response.setOnlineCount(onlineCounts.getOrDefault(chat.getId(), 0));
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
        response.setAttachments(message.getAttachments().stream()
                .map(this::toAttachmentResponse)
                .toList());
        return response;
    }

    private ChatAttachmentResponse toAttachmentResponse(ChatAttachment attachment) {
        ChatAttachmentResponse response = new ChatAttachmentResponse();
        response.setId(attachment.getId());
        response.setFileName(attachment.getFileName());
        response.setStoredName(attachment.getStoredName());
        response.setContentType(attachment.getContentType());
        response.setSize(attachment.getSize());
        response.setUrl("/api/files/chats/" + attachment.getStoredName());
        return response;
    }

    @Transactional
    public ChatSummaryResponse getOrCreateDirectChat(String requesterName, Long targetUserId) {
        User requester = userRepository.findByEmailAndEnabledTrueAndDeletedFalse(requesterName)
                .or(() -> userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(requesterName))
                .orElseThrow(() -> new NotFoundException("User not found: " + requesterName));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Target user not found: " + targetUserId));

        Set<Long> ids = Set.of(requester.getId(), target.getId());
        List<Chat> existing = chatRepository.findDirectChatsBetween(ids);
        Chat chat;
        if (!existing.isEmpty()) {
            chat = existing.get(0);
        } else {
            String title = target.getFirstName() + " " + target.getLastName();
            chat = new Chat();
            chat.setTitle(title.trim());
            chat.setType(org.example.rspcm.model.enums.ChatType.DIRECT);
            chat = chatRepository.save(chat);

            ChatMember m1 = new ChatMember();
            m1.setChat(chat);
            m1.setUser(requester);
            m1.setRole(org.example.rspcm.model.enums.ChatMemberRole.STUDENT);
            chatMemberRepository.save(m1);

            ChatMember m2 = new ChatMember();
            m2.setChat(chat);
            m2.setUser(target);
            m2.setRole(org.example.rspcm.model.enums.ChatMemberRole.STUDENT);
            chatMemberRepository.save(m2);
        }

        ChatSummaryResponse response = new ChatSummaryResponse();
        response.setId(chat.getId());
        response.setTitle(target.getFirstName() + " " + target.getLastName());
        response.setType(chat.getType().name());
        response.setLastMessage("");
        response.setMemberCount(2);
        response.setOnlineCount(0);
        return response;
    }

    @Transactional
    public void addMemberToChat(Long chatId, Long userId, String principalName) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

        if (chat.getType() != ChatType.SUBJECT_GROUP && chat.getType() != ChatType.TEACHER_GROUP) {
            throw new ErrorMessageException(messageService.get("error.chat.no.access"), ErrorCodes.Forbidden);
        }

        User principal = userRepository.findByEmailAndEnabledTrueAndDeletedFalse(principalName)
                .or(() -> userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(principalName))
                .orElseThrow(() -> new NotFoundException("User not found: " + principalName));

        boolean isTeacher = chatMemberRepository.findByChatIdAndUserId(chatId, principal.getId())
                .map(cm -> cm.getRole() == ChatMemberRole.TEACHER)
                .orElse(false);
        if (!isTeacher) {
            throw new ErrorMessageException(messageService.get("error.chat.no.access"), ErrorCodes.Forbidden);
        }

        if (chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
            return; // already a member
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        ChatMember cm = new ChatMember();
        cm.setChat(chat);
        cm.setUser(newMember);
        cm.setRole(ChatMemberRole.STUDENT);
        chatMemberRepository.save(cm);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResponse> getAvailableMembers(Long chatId, String principalName) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

        ensureChatMembership(chatId, principalName);

        Set<Long> existingMemberIds = chatMemberRepository.findByChatId(chatId).stream()
                .map(cm -> cm.getUser().getId())
                .collect(Collectors.toSet());

        if (chat.getStudyGroup() == null) return List.of();

        var group = studyGroupRepository.findById(chat.getStudyGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group not found"));

        java.util.stream.Stream<org.example.rspcm.model.entity.User> candidates;
        if (chat.getType() == ChatType.TEACHER_GROUP) {
            candidates = group.getTeachers().stream();
        } else {
            candidates = java.util.stream.Stream.concat(
                    group.getStudents().stream(),
                    group.getTeachers().stream()
            ).distinct();
        }

        return candidates
                .filter(u -> !existingMemberIds.contains(u.getId()))
                .filter(u -> u.isEnabled() && !u.isDeleted())
                .map(u -> new ChatMemberResponse(u.getId(), u.getFirstName(), u.getLastName(), "STUDENT"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResponse> getChatMembers(Long chatId, String name) {
        ensureChatMembership(chatId, name);
        return chatMemberRepository.findByChatId(chatId).stream()
                .map(cm -> new ChatMemberResponse(
                        cm.getUser().getId(),
                        cm.getUser().getFirstName(),
                        cm.getUser().getLastName(),
                        cm.getRole().name()
                ))
                .toList();
    }

    private void ensureChatMembership(Long chatId, String name) {
        chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

        if (!chatRepository.existsByIdAndMemberIdentifier(chatId, name)) {
            throw new ErrorMessageException(messageService.get("error.chat.no.access"), ErrorCodes.Forbidden);
        }
    }
}
