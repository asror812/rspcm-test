package org.example.rspcm.dto.exam;

import org.example.rspcm.dto.question.QuestionOptionResponse;
import org.example.rspcm.model.enums.QuestionType;

import java.util.List;

public record ExamQuestionResponse(
        Long id,
        Long examId,
        Long questionId,
        String questionText,
        QuestionType questionType,
        Long subjectId,
        String subjectName,
        Integer score,
        Integer orderIndex,
        List<QuestionOptionResponse> options
) {
}
