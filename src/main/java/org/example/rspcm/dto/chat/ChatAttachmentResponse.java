package org.example.rspcm.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatAttachmentResponse {
    private Long id;
    private String fileName;
    private String storedName;
    private String contentType;
    private Long size;
    private String url;
}
