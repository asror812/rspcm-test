package org.example.rspcm.dto.exam;

import org.example.rspcm.dto.common.PracticeSummary;

public record ExamPracticeResponse(
        Long id,
        Long examId,
        PracticeSummary practice) {
}
