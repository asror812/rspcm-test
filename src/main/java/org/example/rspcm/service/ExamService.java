package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamRequest;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.dto.exam.StudentExamListResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.ExamMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamAttemptStatus;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamAttemptRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
import org.example.rspcm.repository.PracticeSubmissionRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final StudyGroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamMapper examMapper;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final PracticeSubmissionRepository practiceSubmissionRepository;
    private final PracticeParticipationRepository practiceParticipationRepository;

    @Transactional(readOnly = true)
    public Page<ExamResponse> findAll(
            User user, String query, ExamType examType, ExamStatus examStatus,
            boolean own, Long subjectId, Pageable pageable) {

        if (isAdmin(user)) {
            return examRepository.searchAll(user.getId(), examType, examStatus, own, subjectId, query, pageable)
                    .map(examMapper::toResponse);
        }

        if (!own) {
            validateTeacherSubjectAccess(user.getId(), subjectId);
        }

        return examRepository.searchAll(user.getId(), examType, examStatus, own, subjectId, query, pageable)
                    .map(examMapper::toResponse);

    }

    @Transactional(readOnly = true)
    public Page<StudentExamListResponse> findMyExams(
            User user,
            String query,
            ExamType examType,
            Long subjectId,
            Pageable pageable
    ) {
        if (!isStudent(user)) {
            throw new ErrorMessageException("Недопустимое действие", ErrorCodes.Forbidden);
        }
        return examRepository.findStudentExams(user.getId(), examType, subjectId, query, pageable)
                .map(examMapper::toResponse)
                .map(this::sanitizeStudentExamResponse)
                .map(r -> StudentExamListResponse.from(r, computeMyStatus(r, user.getId())));
    }

    private String computeMyStatus(ExamResponse exam, Long studentId) {
        if (exam.type() == ExamType.QUESTION) {
            return examAttemptRepository.findByExamIdAndStudentId(exam.id(), studentId)
                    .map(attempt -> switch (attempt.getStatus()) {
                        case STARTED -> "IN_PROGRESS";
                        case SUBMITTED -> "SUBMITTED";
                        case GRADED -> "GRADED";
                        default -> "NOT_STARTED";
                    })
                    .orElse("NOT_STARTED");
        } else {
            // PRACTICE exam
            return practiceSubmissionRepository.findByExamParticipationExamIdAndStudentId(exam.id(), studentId)
                    .map(sub -> switch (sub.getStatus()) {
                        case SUBMITTED -> "SUBMITTED";
                        case RETURNED -> "RETURNED";
                        case GRADED -> "GRADED";
                        default -> "NOT_STARTED";
                    })
                    .orElseGet(() -> practiceParticipationRepository
                            .findByExamIdAndMembersUserId(exam.id(), studentId)
                            .map(p -> "IN_PROGRESS")
                            .orElse("NOT_STARTED"));
        }
    }

    @Transactional(readOnly = true)
    public ExamResponse findById(Long id, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Экзамен не найден: " + id));

        if (isAdmin(user)) {
            return examMapper.toResponse(exam);
        }

        if (isTeacher(user)) {
            validateTeacherSubjectAccess(user.getId(), exam.getSubject() == null ? null : exam.getSubject().getId());
            return examMapper.toResponse(exam);
        }

        if (isStudent(user) && !isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Экзамен не найден: " + id);
        }

        if (isStudent(user)) {
            if (exam.getStatus() != ExamStatus.PUBLISHED) {
                throw new NotFoundException("Экзамен не найден: " + id);
            }
            return sanitizeStudentExamResponse(examMapper.toResponse(exam));
        }

        throw new ErrorMessageException("Недопустимое действие", ErrorCodes.NotFound);
    }

    @Transactional
    public ExamResponse create(User user, ExamRequest request) {
        validateExamRequest(request);
        Subject subject = resolveSubject(request.subjectId());

        validateTeacherSubjectAccess(user.getId(), subject.getId());

        Exam exam = examMapper.toEntity(
                request,
                resolveGroups(request.groupIds()),
                resolveStudents(request.studentIds()),
                user,
                subject
        );

        Exam saved = examRepository.save(exam);
        normalizeExamByType(exam);
        return examMapper.toResponse(examRepository.save(saved));
    }

    @Transactional
    public ExamResponse update(Long id, ExamRequest request, User user) {
        validateExamRequest(request);
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Экзамен не найден: " + id));

        validateTeacherSubjectAccess(user.getId(), exam.getSubject() == null ? null : exam.getSubject().getId());

        Subject subject = resolveSubject(request.subjectId());

        validateTeacherSubjectAccess(user.getId(), subject.getId());

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
    public void delete(Long id, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Экзамен не найден: " + id));

        if (isTeacher(user)) {
            validateTeacherSubjectAccess(user.getId(), exam.getSubject() == null ? null : exam.getSubject().getId());
        }

        examRepository.delete(exam);
    }

    @Transactional
    public ExamResponse updateStatus(Long id, ExamStatus status, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Экзамен не найден: " + id));

        if (isTeacher(user)) {
            validateTeacherSubjectAccess(user.getId(), exam.getSubject() == null ? null : exam.getSubject().getId());
        }

        exam.setStatus(status);
        exam.setUpdatedAt(LocalDateTime.now());
        return examMapper.toResponse(examRepository.save(exam));
    }

    private void validateExamRequest(ExamRequest request) {
        if (request.startAt() != null && request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new ErrorMessageException("endAt должен быть позже startAt", ErrorCodes.BadRequest);
        }
    }

    private void validateTeacherSubjectAccess(Long userId, Long subjectId) {
        boolean teachesSubject = subjectRepository.existsByIdAndTeachersId(subjectId, userId);
        if (!teachesSubject) {
            throw new ErrorMessageException("Вы можете просматривать только экзамены по закреплённым за вами предметам", ErrorCodes.Forbidden);
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
            exam.setPractices(new ArrayList<>());
            return;
        }

        if (exam.getType() == ExamType.PRACTICE) {
            var links = examQuestionRepository.findByExamId(exam.getId());
            if (!links.isEmpty()) {
                examQuestionRepository.deleteAll(links);
            }
            exam.setQuestions(new ArrayList<>());
        }
    }

    private ExamResponse sanitizeStudentExamResponse(ExamResponse response) {
        if (response.status() == ExamStatus.PUBLISHED) {
            return response;
        }
        return new ExamResponse(
                response.id(),
                response.title(),
                response.description(),
                response.startAt(),
                response.endAt(),
                response.maxScore(),
                response.taskLimit(),
                response.type(),
                response.status(),
                response.groups(),
                response.students(),
                List.of(),
                List.of(),
                response.createdBy(),
                response.subject(),
                response.createdAt(),
                response.updatedAt()
        );
    }

    private Subject resolveSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден: " + subjectId));
    }

    private Set<StudyGroup> resolveGroups(Set<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        List<StudyGroup> groups = groupRepository.findAllById(groupIds);
        if (groups.size() != groupIds.size()) {
            throw new NotFoundException("Некоторые группы не найдены");
        }
        return new HashSet<>(groups);
    }

    private Set<User> resolveStudents(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new HashSet<>();
        }
        List<User> students = userRepository.findAllById(studentIds);
        if (students.size() != studentIds.size()) {
            throw new NotFoundException("Некоторые студенты не найдены");
        }
        return new HashSet<>(students);
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
