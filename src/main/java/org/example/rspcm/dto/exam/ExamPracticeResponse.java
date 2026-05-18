package org.example.rspcm.dto.exam;

import org.example.rspcm.dto.common.PracticeSummary;

import java.time.LocalDateTime;

public record ExamPracticeResponse(
        Long id,
        Long examId,
        PracticeSummary practice,
        Integer score,
        Integer orderIndex,
        LocalDateTime deadline
) {
}
