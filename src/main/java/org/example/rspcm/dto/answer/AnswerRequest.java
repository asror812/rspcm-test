package org.example.rspcm.dto.answer;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AnswerRequest(
        @NotNull(message = "Обязательное поле") Long examQuestionId,
        String textAnswer,
        List<Long> selectedOptionIds
) {
}
