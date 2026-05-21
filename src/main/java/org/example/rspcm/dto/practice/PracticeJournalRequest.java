package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PracticeJournalRequest(
        @NotNull Long practiceId,
        @NotNull LocalDate entryDate,
        @NotNull String content,
        String filePath,
        Boolean draft
) {
}
