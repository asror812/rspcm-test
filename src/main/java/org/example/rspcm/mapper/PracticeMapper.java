package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import org.example.rspcm.model.enums.SubmissionType;

@Component
@RequiredArgsConstructor
public class PracticeMapper {
    private final SummaryMapper summaryMapper;

    public PracticeResponse toResponse(Practice practice) {
        UserSummary createdBy = summaryMapper.toUserSummary(practice.getCreatedBy());

        return new PracticeResponse(
                practice.getId(),
                practice.getName(),
                practice.getDescription(),
                practice.getResourceUrl(),
                practice.getRequirements(),
                null,
                practice.getWorkMode(),
                practice.getTeamSize(),
                practice.isSchedulingRequired(),
                practice.getAllowedSubmissionTypes(),
                createdBy
        );
    }

    public Practice toEntity(PracticeRequest request, Set<SubmissionType> submissionTypes, User createdBy) {
        return Practice.builder()
                .name(request.name())
                .description(request.description())
                .resourceUrl(request.resourceUrl())
                .requirements(request.requirements())
                .workMode(request.workMode())
                .teamSize(request.teamSize())
                .schedulingRequired(request.schedulingRequired())
                .allowedSubmissionTypes(submissionTypes)
                .createdBy(createdBy)
                .build();
    }

    public void updateEntity(Practice practice, PracticeRequest request, Set<SubmissionType> submissionTypes) {
        practice.setName(request.name());
        practice.setDescription(request.description());
        practice.setResourceUrl(request.resourceUrl());
        practice.setRequirements(request.requirements());
        practice.setWorkMode(request.workMode());
        practice.setTeamSize(request.teamSize());
        practice.setSchedulingRequired(request.schedulingRequired());
        practice.setAllowedSubmissionTypes(submissionTypes);
    }
}
