package org.example.rspcm.mapper;

import org.example.rspcm.dto.practice.PracticeLogbookEntryResponse;
import org.example.rspcm.model.entity.PracticeLogbookEntry;
import org.springframework.stereotype.Component;

@Component
public class PracticeLogbookEntryMapper {

    public PracticeLogbookEntryResponse toResponse(PracticeLogbookEntry entry) {
        return new PracticeLogbookEntryResponse(
                entry.getId(),
                entry.getEntryDate(),
                entry.getContent(),
                entry.getStatus(),
                entry.getSubmittedAt()
        );
    }
}
