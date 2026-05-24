package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.dto.exam.student.StudentExamAnswerRequest;
import org.example.rspcm.dto.exam.student.StudentExamAttemptResponse;
import org.example.rspcm.dto.exam.student.StudentExamQuestionOptionResponse;
import org.example.rspcm.dto.exam.student.StudentExamQuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.*;
import org.example.rspcm.model.enums.ExamAttemptStatus;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.AnswerRepository;
import org.example.rspcm.repository.ExamAttemptRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.QuestionOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentQuestionExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AnswerRepository answerRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AnswerService answerService;

    @Transactional
    public StudentExamAttemptResponse startAttempt(Long examId, User user) {
        Exam exam = resolvePublishedQuestionExam(examId, user);

        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentId(examId, user.getId())
                .orElseGet(() -> ExamAttempt.builder()
                        .exam(exam)
                        .student(user)
                        .status(ExamAttemptStatus.STARTED)
                        .startedAt(LocalDateTime.now())
                        .build());

        if (attempt.getStatus() == ExamAttemptStatus.SUBMITTED || attempt.getStatus() == ExamAttemptStatus.GRADED) {
            throw new ErrorMessageException("Imtihon allaqachon topshirilgan", ErrorCodes.AlreadyExists);
        }

        if (isExpired(exam)) {
            throw new ErrorMessageException("Imtihon muddati tugagan", ErrorCodes.InvalidParams);
        }

        if (attempt.getId() == null) {
            attempt = examAttemptRepository.save(attempt);
        }
        return toAttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public StudentExamAttemptResponse getMyAttempt(Long examId, User user) {
        resolvePublishedQuestionExam(examId, user);
        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentId(examId, user.getId())
                .orElseThrow(() -> new ErrorMessageException("Imtihon hali boshlanmagan", ErrorCodes.NotFound));
        return toAttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<StudentExamQuestionResponse> getQuestions(Long examId, User user) {
        resolvePublishedQuestionExam(examId, user);
        ExamAttempt attempt = requireStartedAttempt(examId, user.getId());

        if (attempt.getStatus() != ExamAttemptStatus.STARTED) {
            throw new ErrorMessageException("Imtihon topshirilgan", ErrorCodes.InvalidParams);
        }

        List<StudentAnswer> answers = answerRepository.findByStudentIdAndExamQuestionExamId(user.getId(), examId);

        return examQuestionRepository.findByExamId(examId).stream()
                .sorted(Comparator.comparing(ExamQuestion::getOrderIndex))
                .map(eq -> toQuestionResponse(eq, answers))
                .toList();
    }

    @Transactional
    public AnswerResponse saveAnswer(Long examId, Long examQuestionId, StudentExamAnswerRequest request, User user) {
        resolvePublishedQuestionExam(examId, user);
        ExamAttempt attempt = requireStartedAttempt(examId, user.getId());
        if (attempt.getStatus() != ExamAttemptStatus.STARTED) {
            throw new ErrorMessageException("Imtihon topshirilgan", ErrorCodes.InvalidParams);
        }

        if (isExpired(attempt.getExam())) {
            throw new ErrorMessageException("Imtihon muddati tugagan", ErrorCodes.InvalidParams);
        }

        ExamQuestion examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + examQuestionId));
        if (!examQuestion.getExam().getId().equals(examId)) {
            throw new ErrorMessageException("Savol ushbu imtihonga tegishli emas", ErrorCodes.InvalidParams);
        }

        StudentAnswer existing = answerRepository
                .findByStudentIdAndExamQuestionId(user.getId(), examQuestionId)
                .orElse(null);

        org.example.rspcm.dto.answer.AnswerRequest answerRequest = new org.example.rspcm.dto.answer.AnswerRequest(
                examQuestionId,
                request.textAnswer(),
                request.selectedOptionIds()
        );

        if (existing == null) {
            return answerService.createResponse(answerRequest);
        }
        return answerService.update(existing.getId(), answerRequest);
    }

    @Transactional
    public StudentExamAttemptResponse submitAttempt(Long examId, User user) {
        ExamAttempt attempt = requireStartedAttempt(examId, user.getId());
        if (attempt.getStatus() != ExamAttemptStatus.STARTED) {
            throw new ErrorMessageException("Imtihon allaqachon topshirilgan", ErrorCodes.AlreadyExists);
        }
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        return toAttemptResponse(examAttemptRepository.save(attempt));
    }

    private StudentExamQuestionResponse toQuestionResponse(ExamQuestion examQuestion, List<StudentAnswer> answers) {
        StudentAnswer existing = answers.stream()
                .filter(answer -> answer.getExamQuestion().getId().equals(examQuestion.getId()))
                .findFirst()
                .orElse(null);

        List<StudentExamQuestionOptionResponse> options = examQuestion.getQuestion().getOptions() == null
                ? List.of()
                : examQuestion.getQuestion().getOptions().stream()
                .sorted(Comparator.comparing(QuestionOption::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(option -> new StudentExamQuestionOptionResponse(option.getId(), option.getText(), option.getOrderIndex()))
                .toList();

        List<Long> selectedOptionIds = existing == null
                ? List.of()
                : existing.getSelectedOptions().stream().map(link -> link.getQuestionOption().getId()).toList();

        return new StudentExamQuestionResponse(
                examQuestion.getId(),
                examQuestion.getOrderIndex(),
                examQuestion.getQuestion().getType(),
                examQuestion.getQuestion().getText(),
                examQuestion.getScore(),
                options,
                existing == null ? null : existing.getId(),
                existing == null ? null : existing.getTextAnswer(),
                selectedOptionIds
        );
    }

    private Exam resolvePublishedQuestionExam(Long examId, User user) {
        ensureStudent(user);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi: " + examId));
        if (exam.getType() != ExamType.QUESTION) {
            throw new ErrorMessageException("Bu imtihon savol turida emas", ErrorCodes.InvalidParams);
        }
        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new NotFoundException("Imtihon topilmadi: " + examId);
        }
        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Imtihon topilmadi: " + examId);
        }
        return exam;
    }

    private ExamAttempt requireStartedAttempt(Long examId, Long studentId) {
        return examAttemptRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ErrorMessageException("Avval imtihonni boshlang", ErrorCodes.InvalidParams));
    }

    private boolean isAssignedToStudent(Exam exam, Long studentId) {
        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(studentId));
        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(studentId)));
        return assignedDirectly || assignedByGroup;
    }

    private boolean isExpired(Exam exam) {
        return exam.getEndAt() != null && LocalDateTime.now().isAfter(exam.getEndAt());
    }

    private void ensureStudent(User user) {
        boolean isStudent = user.getRoles().stream().map(Role::getRoleName)
                .anyMatch(roleName -> roleName == RoleName.ROLE_STUDENT);
        if (!isStudent) {
            throw new ErrorMessageException("Ruxsat etilmagan amal", ErrorCodes.Forbidden);
        }
    }

    private StudentExamAttemptResponse toAttemptResponse(ExamAttempt attempt) {
        return new StudentExamAttemptResponse(
                attempt.getExam().getId(),
                attempt.getStudent().getId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt()
        );
    }
}
