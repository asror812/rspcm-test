package org.example.rspcm.mapper;

import org.example.rspcm.dto.practice.PracticeJournalResponse;
import org.example.rspcm.model.entity.PracticeLogbook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PracticeJournalMapper {
    private final SummaryMapper summaryMapper;
    private final PracticeLogbookEntryMapper practiceLogbookEntryMapper;

    public PracticeJournalResponse toResponse(PracticeLogbook journal) {
        return new PracticeJournalResponse(
                journal.getId(),
                summaryMapper.toPracticeSummary(journal.getPracticalTask()),
                summaryMapper.toUserSummary(journal.getStudent()),
                journal.getTeam() == null ? null : summaryMapper.toPracticeTeamSummary(journal.getTeam()),
                journal.getFilePath(),
                journal.getSubmittedAt(),
                journal.getStatus(),
                journal.getEntries().stream().map(practiceLogbookEntryMapper::toResponse).toList()
        );
    }
}
