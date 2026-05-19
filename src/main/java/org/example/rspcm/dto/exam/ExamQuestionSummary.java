package org.example.rspcm.dto.exam;

import org.example.rspcm.model.enums.QuestionType;

public record ExamQuestionSummary(
        Long id,
        Long questionId,
        String questionText,
        QuestionType questionType,
        Integer score,
        Integer orderIndex
) {
}
