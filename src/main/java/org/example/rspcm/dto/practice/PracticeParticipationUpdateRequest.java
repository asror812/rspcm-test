package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotNull;

public record PracticeParticipationUpdateRequest(
        @NotNull Long examId,
        Long examPracticeId
) {
}
