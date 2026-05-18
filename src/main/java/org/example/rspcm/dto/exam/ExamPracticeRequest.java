package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ExamPracticeRequest(
        @NotNull Long examId,
        @NotNull Long practiceId,
        @NotNull @Positive Integer score,
        @NotNull @Positive Integer orderIndex,
        LocalDateTime deadline
) {
}
