package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExamPracticeRequest(
        @NotNull Long examId,
        @NotNull Long practiceId,
        @NotNull @Positive Integer orderIndex
) {
}
