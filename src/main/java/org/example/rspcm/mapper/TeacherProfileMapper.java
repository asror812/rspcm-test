package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.profile.TeacherProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileUpdateRequest;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.TeacherProfile;

import java.util.Set;
import java.util.stream.Collectors;

public final class TeacherProfileMapper {
    private TeacherProfileMapper() {
    }

    public static TeacherProfileResponse toResponse(TeacherProfile profile) {
        Set<SubjectSummary> subjects = profile.getTeachingSubjects().stream().map(SummaryMapper::toSubjectSummary).collect(Collectors.toSet());
        return new TeacherProfileResponse(
                profile.getId(),
                SummaryMapper.toUserSummary(profile.getUser()),
                profile.getAcademicDegree(),
                profile.getExperienceYears(),
                subjects
        );
    }

    public static void updateEntity(TeacherProfile profile, TeacherProfileUpdateRequest request, Set<Subject> subjects) {
        profile.setAcademicDegree(request.academicDegree());
        profile.setExperienceYears(request.experienceYears());
        profile.setTeachingSubjects(subjects);
    }
}
