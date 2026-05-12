package org.example.rspcm.dto.question;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.QuestionType;

import java.util.List;

public record QuestionResponse(
        Long id,
        String text,
        QuestionType type,
        SubjectSummary subject,
        UserSummary createdBy,
        List<QuestionOptionResponse> options
) {
}
