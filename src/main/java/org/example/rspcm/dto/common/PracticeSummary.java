package org.example.rspcm.dto.common;

import org.example.rspcm.model.enums.WorkMode;

public record PracticeSummary(
        Long id,
        String name,
        SubjectSummary subject,
        WorkMode workMode,
        Integer teamSize,
        boolean schedulingRequired
) {
}
