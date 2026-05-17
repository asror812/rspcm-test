package org.example.rspcm.dto.profile;

import java.util.Set;

public record TeacherProfileUpdateRequest(
        Set<Long> teachingSubjectIds
) {
}
