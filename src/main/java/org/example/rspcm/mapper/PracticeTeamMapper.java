package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeTeamRequest;
import org.example.rspcm.dto.practice.PracticeTeamResponse;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.PracticeTeam;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PracticeTeamMapper {
    private final SummaryMapper summaryMapper;

    public PracticeTeamResponse toResponse(PracticeTeam team) {
        Set<UserSummary> members = team.getMembers().stream().map(summaryMapper::toUserSummary).collect(Collectors.toSet());
        return new PracticeTeamResponse(team.getId(), summaryMapper.toPracticeSummary(team.getPractice()), team.getName(), members);
    }

    public PracticeTeam toEntity(PracticeTeamRequest request, Practice practice, Set<User> members) {
        return PracticeTeam.builder()
                .practice(practice)
                .name(request.name())
                .members(members)
                .build();
    }
}
