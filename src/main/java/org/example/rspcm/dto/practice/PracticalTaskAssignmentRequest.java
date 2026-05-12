package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record PracticalTaskAssignmentRequest(
        @NotNull Long examId,
        @NotNull Long practicalTaskId,
        Long studentId,
        Long teamId,
        PracticalTaskAssignmentStatus status,
        Integer score,
        String teacherComment
) {
}
