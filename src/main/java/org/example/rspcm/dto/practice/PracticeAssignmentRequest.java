package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.PracticeAssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record PracticeAssignmentRequest(
        @NotNull Long examId,
        @NotNull Long examPracticeId,
        Long studentId,
        PracticeAssignmentStatus status,
        Integer score,
        String teacherComment
) {
}
