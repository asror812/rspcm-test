package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.rspcm.dto.ChatMessageRequest;
import org.example.rspcm.dto.chat.ChatMessageResponse;
import org.example.rspcm.dto.chat.ChatSummaryResponse;
import org.example.rspcm.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/me")
    public List<ChatSummaryResponse> getMyChats(Principal principal) {
        return chatMessageService.getMyChats(principal.getName());
    }

    @GetMapping("/{chatId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long chatId, Principal principal) {
        return chatMessageService.getChatMessages(chatId, principal.getName());
    }

    @PostMapping("/{chatId}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody ChatMessageRequest request,
            Principal principal) {
        return chatMessageService.sendMessage(chatId, request, principal.getName());
    }
}
