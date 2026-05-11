package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.model.entity.PracticalTask;

public final class PracticeMapper {
    private PracticeMapper() {
    }

    public static PracticeResponse toResponse(PracticalTask practicalTask) {
        UserSummary createdBy = SummaryMapper.toUserSummary(practicalTask.getCreatedBy());

        return new PracticeResponse(
                practicalTask.getId(),
                practicalTask.getName(),
                practicalTask.getDescription(),
                practicalTask.getResourceUrl(),
                practicalTask.getRequirements(),
                practicalTask.getDeadline(),
                practicalTask.getWorkMode(),
                practicalTask.getTeamSize(),
                practicalTask.isSchedulingRequired(),
                practicalTask.getAllowedSubmissionTypes(),
                createdBy
        );
    }
}
