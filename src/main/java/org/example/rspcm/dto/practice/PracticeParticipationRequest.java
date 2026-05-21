package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotNull;
import org.example.rspcm.model.enums.PracticeParticipationStatus;

public record PracticeParticipationRequest(
        @NotNull Long examId,
        Long examPracticeId
) {
}
