package org.example.rspcm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    @NotBlank(message = "Сообщение не должно быть пустым")
    @Size(max = 300, message = "Сообщение не должно превышать {max} символов")
    private String message;
}
