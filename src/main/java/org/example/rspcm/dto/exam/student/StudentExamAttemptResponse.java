package org.example.rspcm.dto.exam.student;

import org.example.rspcm.model.enums.ExamAttemptStatus;

import java.time.LocalDateTime;

public record StudentExamAttemptResponse(
        Long examId,
        Long studentId,
        ExamAttemptStatus status,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        LocalDateTime attemptDeadlineAt,
        Long remainingSeconds,
        Integer totalScore
) {
}
