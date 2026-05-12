package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.subject.SubjectRequest;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class SubjectMapper {
    private SubjectMapper() {
    }

    public static SubjectResponse toResponse(Subject subject) {
        Set<UserSummary> teachers = subject.getTeachers().stream()
                .map(SummaryMapper::toUserSummary)
                .collect(Collectors.toSet());
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription(), teachers);
    }

    public static Subject toEntity(SubjectRequest request, Set<User> teachers) {
        return Subject.builder()
                .name(request.name())
                .description(request.description())
                .teachers(teachers)
                .build();
    }

    public static void updateEntity(Subject subject, SubjectRequest request, Set<User> teachers) {
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setTeachers(teachers);
    }
}
