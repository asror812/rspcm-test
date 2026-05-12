package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.SubmissionType;
import org.example.rspcm.model.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.Set;

public record PracticeResponse(
        Long id,
        String name,
        String description,
        String resourceUrl,
        String requirements,
        LocalDateTime deadline,
        WorkMode workMode,
        Integer teamSize,
        boolean calendarRequired,
        Set<SubmissionType> allowedSubmissionTypes,
        UserSummary createdBy
) {
}
