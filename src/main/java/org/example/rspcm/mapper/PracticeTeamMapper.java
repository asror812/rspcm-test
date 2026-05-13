package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeTeamRequest;
import org.example.rspcm.dto.practice.PracticeTeamResponse;
import org.example.rspcm.model.entity.PracticalTask;
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
        return new PracticeTeamResponse(team.getId(), summaryMapper.toPracticeSummary(team.getPracticalTask()), team.getName(), members);
    }

    public PracticeTeam toEntity(PracticeTeamRequest request, PracticalTask practicalTask, Set<User> members) {
        return PracticeTeam.builder()
                .practicalTask(practicalTask)
                .name(request.name())
                .members(members)
                .build();
    }
}
