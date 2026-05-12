package org.example.rspcm.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.Set;

public record ExamRequest(
        @NotBlank String title,
        String description,

        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @NotNull Integer maxScore,
        @NotNull ExamType type,
        Set<Long> groupIds,
        Set<Long> studentIds,
        Long subjectId
) {
}
