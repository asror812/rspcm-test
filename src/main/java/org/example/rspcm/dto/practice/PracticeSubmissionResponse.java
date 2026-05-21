package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;

import java.time.LocalDateTime;

public record PracticeSubmissionResponse(
        Long id,
        Long participationId,
        Long examId,
        Long examPracticeId,
        UserSummary submittedBy,
        String textAnswer,
        String fileUrl,
        LocalDateTime submittedAt,
        PracticeSubmissionStatus status,
        String teacherComment
) {
}
