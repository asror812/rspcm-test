package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotNull;

public record PracticeParticipationCreateRequest(
        @NotNull Long examId
) {
}
