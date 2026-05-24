package org.example.rspcm.dto.exam.student;

public record StudentExamQuestionOptionResponse(
        Long id,
        String text,
        Integer orderIndex
) {
}
