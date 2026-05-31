package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExamPracticeRequest(
        @NotNull(message = "Обязательное поле") Long examId,
        @NotNull(message = "Обязательное поле") Long practiceId,
        @NotNull(message = "Обязательное поле") @Positive(message = "Должно быть положительным числом") Integer orderIndex
) {
}
