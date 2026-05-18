package org.example.rspcm.dto.question;

public record QuestionOptionResponse(
        Long id,
        String text,
        Boolean correct,
        Integer orderIndex
) {
}
