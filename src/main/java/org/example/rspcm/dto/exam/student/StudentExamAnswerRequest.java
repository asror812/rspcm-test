package org.example.rspcm.dto.exam.student;

import java.util.List;

public record StudentExamAnswerRequest(
        String textAnswer,
        List<Long> selectedOptionIds
) {
}
