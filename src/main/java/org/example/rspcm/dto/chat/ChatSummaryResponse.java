package org.example.rspcm.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSummaryResponse {
    private Long id;
    private String title;
    private String type;
    private String lastMessage;
}
