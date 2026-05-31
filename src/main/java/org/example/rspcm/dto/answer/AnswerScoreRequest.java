package org.example.rspcm.dto.answer;

import jakarta.validation.constraints.NotNull;

public record AnswerScoreRequest(
        @NotNull(message = "Обязательное поле") Integer score,
        Boolean correct
) {
}
