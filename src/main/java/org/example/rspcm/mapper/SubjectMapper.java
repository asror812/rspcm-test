package org.example.rspcm.mapper;

import org.example.rspcm.dto.subject.SubjectRequest;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SubjectMapper {
    public SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription());
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
