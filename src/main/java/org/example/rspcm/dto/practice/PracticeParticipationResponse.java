package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.model.enums.PracticeParticipationStatus;

import java.time.LocalDateTime;

public record PracticeParticipationResponse(
        Long id,
        Long examPracticeId,
        Long examId,
        PracticeSummary practice,
        LocalDateTime createdAt,
        PracticeParticipationStatus status
) {
}
