package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamRequest;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.ExamMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.example.rspcm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final StudyGroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamMapper examMapper;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ExamQuestionRepository examQuestionRepository;

    public Page<ExamResponse> findAll(
            User user, String query, ExamType examType,
            boolean own, Long subjectId, Pageable pageable) {

        if (isAdmin(user)) {
            return examRepository.searchAll(user.getId(), examType, own, subjectId, query, pageable)
                    .map(examMapper::toResponse);
        }

        if (isTeacher(user)) {
            validateTeacherSubjectAccess(user.getId(), subjectId);

            return examRepository.searchAll(user.getId(), examType, own, subjectId, query, pageable)
                    .map(examMapper::toResponse);
        }

        throw new ErrorMessageException("Ruxsat etilmagan amal", ErrorCodes.Forbidden);
    }

    public ExamResponse findById(Long id, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi: " + id));

        if (isAdmin(user)) {
            return examMapper.toResponse(exam);
        }

        if (isTeacher(user)) {
            validateTeacherSubjectAccess(user.getId(), exam.getId());
            return examMapper.toResponse(exam);
        }

        if (isStudent(user) && !isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Imtihon topilmadi: " + id);
        }

        throw new ErrorMessageException("Ruxsat etilmagan amal", ErrorCodes.NotFound);
    }

    @Transactional
    public ExamResponse create(User user, ExamRequest request) {
        Subject subject = resolveSubject(request.subjectId());
        Exam exam = examMapper.toEntity(
                request,
                resolveGroups(request.groupIds()),
                resolveStudents(request.studentIds()),
                user,
                subject
        );
        Exam saved = examRepository.save(exam);
        normalizeExamByType(saved);
        return examMapper.toResponse(examRepository.save(saved));
    }

    @Transactional
    public ExamResponse update(Long id, ExamRequest request, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi: " + id));

        validateTeacherSubjectAccess(user.getId(), exam.getId());

        Subject subject = resolveSubject(request.subjectId());

        examMapper.updateEntity(
                exam,
                request,
                resolveGroups(request.groupIds()),
                resolveStudents(request.studentIds()),
                subject
        );

        normalizeExamByType(exam);
        return examMapper.toResponse(examRepository.save(exam));
    }

    @Transactional
    public void delete(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi: " + id));

        examRepository.delete(exam);
    }

    private void validateTeacherSubjectAccess(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException("Fan bo'yicha filtr kiritilishi shart", ErrorCodes.BadRequest);
        }

        boolean teachesSubject = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(userId, subjectId);
        if (!teachesSubject) {
            throw new ErrorMessageException("Faqat o'zingizga biriktirilgan fan imtihonlarini ko'ra olasiz", ErrorCodes.Forbidden);
        }
    }

    private boolean isAssignedToStudent(Exam exam, Long studentId) {
        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(studentId));

        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(studentId)));

        return assignedDirectly || assignedByGroup;
    }

    private void normalizeExamByType(Exam exam) {
        if (exam.getType() == ExamType.QUESTION) {
            exam.setPracticalTasks(new HashSet<>());
            return;
        }

        if (exam.getType() == ExamType.PRACTICAL_TASK) {
            var links = examQuestionRepository.findByExamId(exam.getId());
            if (!links.isEmpty()) {
                examQuestionRepository.deleteAll(links);
            }
            exam.setQuestions(new ArrayList<>());
        }
    }

    private Subject resolveSubject(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Fan topilmadi: " + subjectId));
    }

    private Set<StudyGroup> resolveGroups(Set<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(groupRepository.findAllById(groupIds));
    }

    private Set<User> resolveStudents(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(userRepository.findAllById(studentIds));
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == roleName);
    }

    private boolean isAdmin(User user) {
        return hasRole(user, RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return hasRole(user, RoleName.ROLE_TEACHER);
    }

    private boolean isStudent(User user) {
        return hasRole(user, RoleName.ROLE_STUDENT);
    }
}
