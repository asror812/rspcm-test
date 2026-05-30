package org.example.rspcm.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChatMessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private boolean mine;
    private List<ChatAttachmentResponse> attachments = new ArrayList<>();
}
