package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.PracticeTeamSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.LogbookStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PracticeJournalResponse(
        Long id,
        PracticeSummary practice,
        UserSummary student,
        PracticeTeamSummary team,
        String filePath,
        LocalDateTime submittedAt,
        LogbookStatus status,
        List<PracticeLogbookEntryResponse> entries
) {
}
