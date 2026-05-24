package org.example.rspcm.dto;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRecentReportResponse(
        Long submissionId,
        Long examId,
        String examTitle,
        List<String> groupNames,
        UserSummary submittedBy,
        PracticeSubmissionStatus status,
        LocalDateTime submittedAt
) {
}
