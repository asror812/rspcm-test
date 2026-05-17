package org.example.rspcm.dto.profile;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;

import java.util.Set;

public record TeacherProfileResponse(
        Long id,
        UserSummary user,
        String academicDegree,
        Set<SubjectSummary> teachingSubjects
) {
}
