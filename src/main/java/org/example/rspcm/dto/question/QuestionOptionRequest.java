package org.example.rspcm.dto.question;

import jakarta.validation.constraints.NotBlank;

public record QuestionOptionRequest(
        @NotBlank(message = "Поле не должно быть пустым") String text,
        boolean correct,
        Integer orderIndex
) {
}
