package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.model.enums.PracticeParticipationStatus;

import java.util.List;

public record MyPracticeParticipationResponse(
        Long participationId,
        Long examId,
        Long examPracticeId,
        PracticeSummary practice,
        PracticeParticipationStatus status,
        List<PracticeParticipationMemberResponse> members,
        PracticeSubmissionResponse submission
) {
}
