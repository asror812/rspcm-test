package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.LogbookEntryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PracticeLogbookEntryResponse(
        Long id,
        LocalDate entryDate,
        String content,
        LogbookEntryStatus status,
        LocalDateTime submittedAt
) {
}
