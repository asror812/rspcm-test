package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.ExamSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.PracticeTeamSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.PracticeAssignmentStatus;

import java.time.LocalDateTime;

public record PracticeAssignmentResponse(
        Long id,
        ExamSummary exam,
        PracticeSummary practice,
        UserSummary student,
        PracticeTeamSummary team,
        LocalDateTime chosenAt,
        LocalDateTime submittedAt,
        PracticeAssignmentStatus status,
        Integer score,
        String teacherComment
) {
}
