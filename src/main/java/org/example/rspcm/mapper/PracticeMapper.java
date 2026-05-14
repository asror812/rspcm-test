package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import org.example.rspcm.model.enums.SubmissionType;

@Component
@RequiredArgsConstructor
public class PracticeMapper {
    private final SummaryMapper summaryMapper;

    public PracticeResponse toResponse(PracticalTask practicalTask) {
        UserSummary createdBy = summaryMapper.toUserSummary(practicalTask.getCreatedBy());

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

    public PracticalTask toEntity(PracticeRequest request, Set<SubmissionType> submissionTypes, User createdBy) {
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

    public void updateEntity(PracticalTask practicalTask, PracticeRequest request, Set<SubmissionType> submissionTypes) {
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
