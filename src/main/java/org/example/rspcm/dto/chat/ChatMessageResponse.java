package org.example.rspcm.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private boolean mine;
}
