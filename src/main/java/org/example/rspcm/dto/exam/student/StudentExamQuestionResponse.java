package org.example.rspcm.dto.exam.student;

import org.example.rspcm.model.enums.QuestionType;

import java.util.List;

public record StudentExamQuestionResponse(
        Long examQuestionId,
        Integer orderIndex,
        QuestionType questionType,
        String questionText,
        Integer score,
        List<StudentExamQuestionOptionResponse> options,
        Long answerId,
        String textAnswer,
        List<Long> selectedOptionIds
) {
}
