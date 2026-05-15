package org.example.rspcm.service;

import org.example.rspcm.dto.profile.StudentProfileUpdateRequest;
import org.example.rspcm.dto.profile.StudentProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileResponse;
import org.example.rspcm.dto.profile.TeacherSelfProfileUpdateRequest;
import org.example.rspcm.dto.profile.TeacherProfileUpdateRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.StudentProfileMapper;
import org.example.rspcm.mapper.TeacherProfileMapper;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.StudentProfile;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.TeacherProfile;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final StudyGroupRepository studyGroupRepository;
    private final SubjectRepository subjectRepository;
    private final StudentProfileMapper studentProfileMapper;
    private final TeacherProfileMapper teacherProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public StudentProfile getStudentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile topilmadi: " + userId));
    }

    public StudentProfileResponse getStudentProfileResponse(Long userId) {
        return studentProfileMapper.toResponse(studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile topilmadi: " + userId)));
    }

    public TeacherProfile getTeacherProfile(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Teacher profile topilmadi: " + userId));
    }

    public TeacherProfileResponse getTeacherProfileResponse(Long userId) {
        return teacherProfileMapper.toResponse(teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Teacher profile topilmadi: " + userId)));
    }

    @Transactional
    public StudentProfile updateStudentProfile(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(StudentProfile.builder().user(getUser(userId)).build()));
        studentProfileMapper.updateEntity(profile, request);
        profile.setGroup(resolveGroup(request.groupId()));
        return studentProfileRepository.save(profile);
    }

    public StudentProfileResponse updateStudentProfileResponse(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(StudentProfile.builder().user(getUser(userId)).build()));
        studentProfileMapper.updateEntity(profile, request);
        profile.setGroup(resolveGroup(request.groupId()));
        return studentProfileMapper.toResponse(studentProfileRepository.save(profile));
    }

    public StudentProfileResponse updateMyStudentProfileResponse(Long userId, StudentProfileUpdateRequest request) {
        if (request.groupId() != null) {
            throw new ErrorMessageException("Talaba o'z guruhini o'zgartira olmaydi", ErrorCodes.InvalidParams);
        }
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> studentProfileRepository.save(StudentProfile.builder().user(getUser(userId)).build()));
        studentProfileMapper.updateEntity(profile, request);
        return studentProfileMapper.toResponse(studentProfileRepository.save(profile));
    }

    @Transactional
    public TeacherProfile updateTeacherProfile(Long userId, TeacherProfileUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseGet(() -> teacherProfileRepository.save(TeacherProfile.builder().user(getUser(userId)).build()));
        teacherProfileMapper.updateEntity(profile, request, resolveSubjects(request.teachingSubjectIds()));
        return teacherProfileRepository.save(profile);
    }

    public TeacherProfileResponse updateTeacherProfileResponse(Long userId, TeacherProfileUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseGet(() -> teacherProfileRepository.save(
                        TeacherProfile.builder()
                                .user(getUser(userId))
                                .build()));

        teacherProfileMapper.updateEntity(profile, request, resolveSubjects(request.teachingSubjectIds()));
        return teacherProfileMapper.toResponse(teacherProfileRepository.save(profile));
    }

    @Transactional
    public TeacherProfileResponse updateMyTeacherProfileResponse(Long userId, TeacherSelfProfileUpdateRequest request) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseGet(() -> teacherProfileRepository.save(
                        TeacherProfile.builder()
                                .user(getUser(userId))
                                .build()));

        User user = profile.getUser();
        updateSelfEditableUserFields(user, request);
        teacherProfileMapper.updateSelfEditableFields(profile, request.academicDegree(), request.experienceYears());

        userRepository.save(user);
        return teacherProfileMapper.toResponse(teacherProfileRepository.save(profile));
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

    private StudyGroup resolveGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group topilmadi: " + groupId));
    }

    private void updateSelfEditableUserFields(User user, TeacherSelfProfileUpdateRequest request) {
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ErrorMessageException("Bu email allaqachon mavjud", ErrorCodes.AlreadyExists);
            }
            user.setEmail(request.email());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }

        if (request.newPassword() != null) {
            if (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new ErrorMessageException("Joriy parol noto'g'ri", ErrorCodes.InvalidParams);
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }
    }
}
