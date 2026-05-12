package org.example.rspcm.service;

import org.example.rspcm.dto.profile.StudentProfileUpdateRequest;
import org.example.rspcm.dto.profile.StudentProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileUpdateRequest;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.StudentProfileMapper;
import org.example.rspcm.mapper.TeacherProfileMapper;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.StudentProfile;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.TeacherProfile;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public StudentProfile getStudentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile topilmadi: " + userId));
    }

    public StudentProfileResponse getStudentProfileResponse(Long userId) {
        return StudentProfileMapper.toResponse(studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile topilmadi: " + userId)));
    }

    public TeacherProfile getTeacherProfile(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Teacher profile topilmadi: " + userId));
    }

    public TeacherProfileResponse getTeacherProfileResponse(Long userId) {
        return TeacherProfileMapper.toResponse(teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Teacher profile topilmadi: " + userId)));
    }

    @Transactional
    public StudentProfile updateStudentProfile(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(StudentProfile.builder().user(getUser(userId)).build()));
        StudentProfileMapper.updateEntity(profile, request);
        return studentProfileRepository.save(profile);
    }

    public StudentProfileResponse updateStudentProfileResponse(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(StudentProfile.builder().user(getUser(userId)).build()));
        StudentProfileMapper.updateEntity(profile, request);
        return StudentProfileMapper.toResponse(studentProfileRepository.save(profile));
    }

    @Transactional
    public TeacherProfile updateTeacherProfile(Long userId, TeacherProfileUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseGet(() -> teacherProfileRepository.save(TeacherProfile.builder().user(getUser(userId)).build()));
        TeacherProfileMapper.updateEntity(profile, request, resolveSubjects(request.teachingSubjectIds()));
        return teacherProfileRepository.save(profile);
    }

    public TeacherProfileResponse updateTeacherProfileResponse(Long userId, TeacherProfileUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseGet(() -> teacherProfileRepository.save(TeacherProfile.builder().user(getUser(userId)).build()));
        TeacherProfileMapper.updateEntity(profile, request, resolveSubjects(request.teachingSubjectIds()));
        return TeacherProfileMapper.toResponse(teacherProfileRepository.save(profile));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User topilmadi: " + userId));
    }

    private Set<Subject> resolveSubjects(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(subjectRepository.findAllById(ids));
    }
}
