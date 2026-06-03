package org.example.rspcm.dto.practice;

import java.time.LocalDateTime;

public record PracticeSubmissionAttemptResponse(
        Long id,
        int attemptNumber,
        String textAnswer,
        String fileUrl,
        LocalDateTime submittedAt,
        String teacherComment
) {
}
