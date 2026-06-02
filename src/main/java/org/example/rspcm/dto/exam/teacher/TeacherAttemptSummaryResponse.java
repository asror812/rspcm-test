package org.example.rspcm.dto.exam.teacher;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.ExamAttemptStatus;

import java.time.LocalDateTime;

public record TeacherAttemptSummaryResponse(
        Long attemptId,
        UserSummary student,
        ExamAttemptStatus status,
        LocalDateTime submittedAt,
        Integer totalScore,
        int openUngradedCount
) {
}
