package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.rspcm.dto.ChatMessageRequest;
import org.example.rspcm.dto.chat.ChatMessageResponse;
import org.example.rspcm.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebsocketController {
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chats/{id}/messages")
    public ChatMessageResponse sendMessage(
            @DestinationVariable Long chatId,
            @Valid ChatMessageRequest request,
            Principal principal) {
        return chatMessageService.sendMessage(chatId, request, principal.getName());
    }
}
