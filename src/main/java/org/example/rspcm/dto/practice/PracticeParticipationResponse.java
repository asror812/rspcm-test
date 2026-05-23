package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.PracticeParticipationStatus;

import java.time.LocalDateTime;

public record PracticeParticipationResponse(
        Long id,
        Long examPracticeId,
        Long examId,
        PracticeParticipationPracticeSummary practice,
        LocalDateTime createdAt,
        PracticeParticipationStatus status
) {
}
