package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.*;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.Set;

public record ExamRequest(
        @NotBlank String title,
        String description,

        @NotNull @FutureOrPresent LocalDateTime startAt,
        @NotNull @Future LocalDateTime endAt,
        @NotNull @Positive Integer maxScore,
        @NotNull @Positive Integer itemLimit,

        @NotNull ExamType type,
        Set<Long> groupIds,
        Set<Long> studentIds,
        Long subjectId
) {
}
