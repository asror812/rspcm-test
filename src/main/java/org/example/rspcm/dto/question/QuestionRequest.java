package org.example.rspcm.dto.question;

import org.example.rspcm.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionRequest(
        @NotBlank(message = "Поле не должно быть пустым") String text,
        @NotNull(message = "Обязательное поле") QuestionType type,
        @NotNull(message = "Обязательное поле") Long subjectId,
        List<QuestionOptionRequest> options
) {
}
