package org.example.rspcm.mapper;

import org.example.rspcm.dto.practice.PracticeJournalResponse;
import org.example.rspcm.model.entity.PracticeLogbook;

public final class PracticeJournalMapper {
    private PracticeJournalMapper() {
    }

    public static PracticeJournalResponse toResponse(PracticeLogbook journal) {
        return new PracticeJournalResponse(
                journal.getId(),
                SummaryMapper.toPracticeSummary(journal.getPracticalTask()),
                SummaryMapper.toUserSummary(journal.getStudent()),
                journal.getTeam() == null ? null : SummaryMapper.toPracticeTeamSummary(journal.getTeam()),
                journal.getFilePath(),
                journal.getSubmittedAt(),
                journal.getStatus(),
                journal.getEntries().stream().map(PracticeLogbookEntryMapper::toResponse).toList()
        );
    }
}
