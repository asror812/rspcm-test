package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.PracticeParticipationStatus;

public record PracticeParticipationStatusUpdateRequest(
        PracticeParticipationStatus status
) {
}
