package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.group.GroupRequest;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.dto.group.GroupResponse;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupMapper {
    private final SummaryMapper summaryMapper;

    public GroupResponse toResponse(StudyGroup group) {
        Set<SubjectSummary> subjects = group.getSubjects().stream().map(summaryMapper::toSubjectSummary).collect(Collectors.toSet());
        Set<UserSummary> teachers = group.getTeachers().stream().map(summaryMapper::toUserSummary).collect(Collectors.toSet());
        Set<UserSummary> students = group.getStudents().stream().map(summaryMapper::toUserSummary).collect(Collectors.toSet());
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(), group.getLanguage(), subjects, teachers, students);
    }

    public StudyGroup toEntity(
            GroupRequest request,
            Set<Subject> subjects,
            Set<User> teachers,
            Set<User> students
    ) {
        return StudyGroup.builder()
                .name(request.name())
                .description(request.description())
                .language(request.language())
                .subjects(subjects)
                .teachers(teachers)
                .students(students)
                .build();
    }

    public void updateEntity(
            StudyGroup group,
            GroupRequest request,
            Set<Subject> subjects,
            Set<User> teachers,
            Set<User> students
    ) {
        group.setName(request.name());
        group.setDescription(request.description());
        group.setLanguage(request.language());
        group.setSubjects(subjects);
        group.setTeachers(teachers);
        group.setStudents(students);
    }
}
