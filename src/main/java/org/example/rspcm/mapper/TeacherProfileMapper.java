package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.profile.TeacherProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileUpdateRequest;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.TeacherProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherProfileMapper {
    private final SummaryMapper summaryMapper;

    public TeacherProfileResponse toResponse(TeacherProfile profile) {
        Set<SubjectSummary> subjects = profile.getTeachingSubjects().stream().map(summaryMapper::toSubjectSummary).collect(Collectors.toSet());
        return new TeacherProfileResponse(
                profile.getId(),
                summaryMapper.toUserSummary(profile.getUser()),
                profile.getAcademicDegree(),
                profile.getExperienceYears(),
                subjects
        );
    }

    public void updateEntity(TeacherProfile profile, TeacherProfileUpdateRequest request, Set<Subject> subjects) {
        profile.setAcademicDegree(request.academicDegree());
        profile.setExperienceYears(request.experienceYears());
        profile.setTeachingSubjects(subjects);
    }

    public void updateSelfEditableFields(TeacherProfile profile, String academicDegree, Integer experienceYears) {
        profile.setAcademicDegree(academicDegree);
        profile.setExperienceYears(experienceYears);
    }
}
