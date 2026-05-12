package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.ExamSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.PracticeTeamSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;

import java.time.LocalDateTime;

public record PracticalTaskAssignmentResponse(
        Long id,
        ExamSummary exam,
        PracticeSummary practicalTask,
        UserSummary student,
        PracticeTeamSummary team,
        LocalDateTime chosenAt,
        LocalDateTime submittedAt,
        PracticalTaskAssignmentStatus status,
        Integer score,
        String teacherComment
) {
}
