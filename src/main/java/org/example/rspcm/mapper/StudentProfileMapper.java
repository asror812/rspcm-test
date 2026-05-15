package org.example.rspcm.mapper;

import org.example.rspcm.dto.profile.StudentProfileUpdateRequest;
import org.example.rspcm.dto.profile.StudentProfileResponse;
import org.example.rspcm.model.entity.StudentProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentProfileMapper {
    private final SummaryMapper summaryMapper;

    public StudentProfileResponse toResponse(StudentProfile profile) {
        return new StudentProfileResponse(
                profile.getId(),
                summaryMapper.toUserSummary(profile.getUser()),
                profile.getCourse(),
                profile.getGroup() == null ? null : summaryMapper.toGroupSummary(profile.getGroup())
        );
    }

    public void updateEntity(StudentProfile profile, StudentProfileUpdateRequest request) {
        profile.setCourse(request.course());
    }
}
