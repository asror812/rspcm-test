package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.*;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.Set;

public record ExamRequest(
        @NotBlank(message = "Поле не должно быть пустым") String title,
        String description,

        @NotNull(message = "Обязательное поле") @FutureOrPresent(message = "Дата должна быть сейчас или в будущем") LocalDateTime startAt,
        @NotNull(message = "Обязательное поле") @Future(message = "Дата должна быть в будущем") LocalDateTime endAt,
        @NotNull(message = "Обязательное поле") @Positive(message = "Должно быть положительным числом") Integer maxScore,
        @NotNull(message = "Обязательное поле") @Positive(message = "Должно быть положительным числом") Integer taskLimit,

        @NotNull(message = "Обязательное поле") ExamType type,
        @NotNull(message = "Обязательное поле") Long subjectId,
        Set<Long> groupIds,
        Set<Long> studentIds
) {
}
