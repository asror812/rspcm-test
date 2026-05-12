package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.model.enums.SubmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public record PracticeRequest(
        @NotBlank String name,
        String description,
        String resourceUrl,
        String requirements,
        LocalDateTime deadline,
        @NotNull WorkMode workMode,
        Integer teamSize,
        boolean schedulingRequired,
        Set<SubmissionType> allowedSubmissionTypes,
        Set<Long> groupIds,
        Set<Long> studentIds,
        Long subjectId
) {
}
