package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExamQuestionRequest(
        @NotNull(message = "Обязательное поле") Long examId,
        @NotNull(message = "Обязательное поле") Long questionId,
        @NotNull(message = "Обязательное поле") @Positive(message = "Значение должно быть положительным") Integer score,
        @NotNull(message = "Обязательное поле") Integer orderIndex
) {
}
