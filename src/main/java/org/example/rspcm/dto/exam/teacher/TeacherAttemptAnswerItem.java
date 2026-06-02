package org.example.rspcm.dto.exam.teacher;

import org.example.rspcm.model.enums.QuestionType;

import java.util.List;

public record TeacherAttemptAnswerItem(
        Long answerId,
        Long examQuestionId,
        int orderIndex,
        String questionText,
        QuestionType questionType,
        int maxScore,
        String textAnswer,
        List<Long> selectedOptionIds,
        List<TeacherAnswerOptionItem> options,
        Integer score,
        Boolean correct
) {
}
