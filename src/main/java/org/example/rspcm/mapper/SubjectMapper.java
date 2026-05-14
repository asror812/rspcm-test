package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.subject.SubjectRequest;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubjectMapper {
    private final SummaryMapper summaryMapper;

    public SubjectResponse toResponse(Subject subject) {
        Set<UserSummary> teachers = subject.getTeachers().stream()
                .map(summaryMapper::toUserSummary)
                .collect(Collectors.toSet());
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription(), teachers);
    }

    public Subject toEntity(SubjectRequest request, Set<User> teachers) {
        return Subject.builder()
                .name(request.name())
                .description(request.description())
                .teachers(teachers)
                .build();
    }

    public void updateEntity(Subject subject, SubjectRequest request, Set<User> teachers) {
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setTeachers(teachers);
    }
}
