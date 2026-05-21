package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotNull;

public record PracticeParticipationRequest(
        @NotNull Long examId,
        Long examPracticeId
) {
}
