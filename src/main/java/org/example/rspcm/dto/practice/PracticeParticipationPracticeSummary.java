package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.WorkMode;

public record PracticeParticipationPracticeSummary(
        Long id,
        String name,
        WorkMode workMode,
        Integer teamSize,
        boolean schedulingRequired
) {
}
