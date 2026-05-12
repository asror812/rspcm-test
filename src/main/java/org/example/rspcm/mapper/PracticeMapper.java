package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.User;

import java.util.Set;
import org.example.rspcm.model.enums.SubmissionType;

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

    public static PracticalTask toEntity(PracticeRequest request, Set<SubmissionType> submissionTypes, User createdBy) {
        return PracticalTask.builder()
                .name(request.name())
                .description(request.description())
                .resourceUrl(request.resourceUrl())
                .requirements(request.requirements())
                .deadline(request.deadline())
                .workMode(request.workMode())
                .teamSize(request.teamSize())
                .schedulingRequired(request.schedulingRequired())
                .allowedSubmissionTypes(submissionTypes)
                .createdBy(createdBy)
                .build();
    }

    public static void updateEntity(PracticalTask practicalTask, PracticeRequest request, Set<SubmissionType> submissionTypes) {
        practicalTask.setName(request.name());
        practicalTask.setDescription(request.description());
        practicalTask.setResourceUrl(request.resourceUrl());
        practicalTask.setRequirements(request.requirements());
        practicalTask.setDeadline(request.deadline());
        practicalTask.setWorkMode(request.workMode());
        practicalTask.setTeamSize(request.teamSize());
        practicalTask.setSchedulingRequired(request.schedulingRequired());
        practicalTask.setAllowedSubmissionTypes(submissionTypes);
    }
}
