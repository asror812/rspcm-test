package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExamQuestionRequest(
        @NotNull Long examId,
        @NotNull Long questionId,
        @NotNull @Positive(message = "Score 0 dan katta bo'lishi kerak") Integer score,
        @NotNull Integer orderIndex
) {
}
