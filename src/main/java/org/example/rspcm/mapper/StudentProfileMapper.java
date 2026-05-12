package org.example.rspcm.mapper;

import org.example.rspcm.dto.profile.StudentProfileUpdateRequest;
import org.example.rspcm.dto.profile.StudentProfileResponse;
import org.example.rspcm.model.entity.StudentProfile;

public final class StudentProfileMapper {
    private StudentProfileMapper() {
    }

    public static StudentProfileResponse toResponse(StudentProfile profile) {
        return new StudentProfileResponse(
                profile.getId(),
                SummaryMapper.toUserSummary(profile.getUser()),
                profile.getCourse(),
                profile.getStudentNumber(),
                profile.getPhoneNumber(),
                profile.getNotes()
        );
    }

    public static void updateEntity(StudentProfile profile, StudentProfileUpdateRequest request) {
        profile.setCourse(request.course());
        profile.setStudentNumber(request.studentNumber());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setNotes(request.notes());
    }
}
