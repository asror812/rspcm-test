package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamPracticeRequest;
import org.example.rspcm.dto.exam.ExamPracticeResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamPracticeService {

    private final ExamPracticeRepository examPracticeRepository;
    private final ExamRepository examRepository;
    private final PracticeRepository practiceRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SummaryMapper summaryMapper;
    private final MessageService messageService;

    @Transactional(readOnly = true)
    public Page<ExamPracticeResponse> findAll(Long examId, User user, Pageable pageable) {
        if (examId == null) {
            throw new ErrorMessageException(messageService.get("error.exam.id.required"), ErrorCodes.BadRequest);
        }

        Exam exam = resolveExam(examId);
        validateTeacherAccess(user, exam);
        return examPracticeRepository.findByExamId(examId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ExamPracticeResponse> findAllForStudent(Long examId, User user) {
        Exam exam = resolveExam(examId);

        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException(messageService.get("error.exam.type.not.practice"), ErrorCodes.BadRequest);
        }

        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }

        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }

        return examPracticeRepository.findByExamId(examId, Pageable.unpaged())
                .map(this::toResponse)
                .getContent();
    }

    @Transactional(readOnly = true)
    public ExamPracticeResponse findById(Long id, User user) {
        ExamPractice examPractice = findEntityById(id);
        validateTeacherAccess(user, examPractice.getExam());
        return toResponse(examPractice);
    }

    @Transactional
    public ExamPracticeResponse create(ExamPracticeRequest request, User user) {
        Exam exam = resolveExam(request.examId());
        validateTeacherAccess(user, exam);
        validateExamType(exam);

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException(messageService.get("error.practice.not.found", request.practiceId())));

        if (examPracticeRepository.existsByExamIdAndPracticeId(exam.getId(), practice.getId())) {
            throw new ErrorMessageException(messageService.get("error.practice.already.linked"), ErrorCodes.AlreadyExists);
        }

        ExamPractice link = ExamPractice.builder()
                .exam(exam)
                .practice(practice)
                .build();

        return toResponse(examPracticeRepository.save(link));
    }

    @Transactional
    public ExamPracticeResponse update(Long id, ExamPracticeRequest request, User user) {
        ExamPractice existing = findEntityById(id);
        validateTeacherAccess(user, existing.getExam());

        Exam exam = resolveExam(request.examId());
        validateTeacherAccess(user, exam);
        validateExamType(exam);

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException(messageService.get("error.practice.not.found", request.practiceId())));

        if (examPracticeRepository.existsByExamIdAndPracticeIdAndIdNot(exam.getId(), practice.getId(), existing.getId())) {
            throw new ErrorMessageException(messageService.get("error.practice.already.linked"), ErrorCodes.AlreadyExists);
        }

        existing.setExam(exam);
        existing.setPractice(practice);
        return toResponse(examPracticeRepository.save(existing));
    }

    @Transactional
    public void delete(Long id, User user) {
        ExamPractice existing = findEntityById(id);
        validateTeacherAccess(user, existing.getExam());
        examPracticeRepository.delete(existing);
    }

    private ExamPractice findEntityById(Long id) {
        return examPracticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExamПрактика не найдена: " + id));
    }

    private Exam resolveExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));
    }

    private void validateExamType(Exam exam) {
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException(messageService.get("error.exam.not.practice.type"), ErrorCodes.BadRequest);
        }
    }

    private void validateTeacherAccess(User user, Exam exam) {
        if (isAdmin(user)) {
            return;
        }
        if (!isTeacher(user)) {
            throw new ErrorMessageException(messageService.get("error.forbidden"), ErrorCodes.Forbidden);
        }

        if (exam.getSubject() == null) {
            throw new ErrorMessageException(messageService.get("error.exam.no.subject"), ErrorCodes.BadRequest);
        }

        boolean teaches = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), exam.getSubject().getId());
        if (!teaches) {
            throw new ErrorMessageException(messageService.get("error.exam.access.denied"), ErrorCodes.Forbidden);
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_TEACHER);
    }

    private boolean isAssignedToStudent(Exam exam, Long studentId) {
        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(studentId));

        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(studentId)));

        return assignedDirectly || assignedByGroup;
    }

    private ExamPracticeResponse toResponse(ExamPractice link) {
        return new ExamPracticeResponse(
                link.getId(),
                link.getExam().getId(),
                summaryMapper.toPracticeSummary(link.getPractice())
        );
    }
}
