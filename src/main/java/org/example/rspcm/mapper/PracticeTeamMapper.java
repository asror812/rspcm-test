package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.practice.PracticeTeamRequest;
import org.example.rspcm.dto.practice.PracticeTeamResponse;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.PracticeTeam;
import org.example.rspcm.model.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class PracticeTeamMapper {
    private PracticeTeamMapper() {
    }

    public static PracticeTeamResponse toResponse(PracticeTeam team) {
        Set<UserSummary> members = team.getMembers().stream().map(SummaryMapper::toUserSummary).collect(Collectors.toSet());
        return new PracticeTeamResponse(team.getId(), SummaryMapper.toPracticeSummary(team.getPracticalTask()), team.getName(), members);
    }

    public static PracticeTeam toEntity(PracticeTeamRequest request, PracticalTask practicalTask, Set<User> members) {
        return PracticeTeam.builder()
                .practicalTask(practicalTask)
                .name(request.name())
                .members(members)
                .build();
    }
}
