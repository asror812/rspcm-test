package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.answer.AnswerRequest;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.dto.exam.student.StudentExamAnswerRequest;
import org.example.rspcm.dto.exam.student.StudentExamAttemptResponse;
import org.example.rspcm.dto.exam.student.StudentExamQuestionOptionResponse;
import org.example.rspcm.dto.exam.student.StudentExamQuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.AnswerMapper;
import org.example.rspcm.model.entity.*;
import org.example.rspcm.model.enums.ExamAttemptStatus;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.QuestionType;
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

@Service
@RequiredArgsConstructor
public class StudentQuestionExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AnswerRepository answerRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AnswerMapper answerMapper;
    private final MessageService messageService;

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
            throw new ErrorMessageException(messageService.get("error.exam.already.submitted"), ErrorCodes.AlreadyExists);
        }

        if (isExpired(exam)) {
            throw new ErrorMessageException(messageService.get("error.exam.deadline.passed"), ErrorCodes.InvalidParams);
        }

        if (attempt.getId() == null) {
            attempt = examAttemptRepository.save(attempt);
        }
        if (attempt.getStatus() == ExamAttemptStatus.STARTED && isAttemptExpired(attempt)) {
            attempt = finalizeAttempt(attempt);
        }
        return toAttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public StudentExamAttemptResponse getMyAttempt(Long examId, User user) {
        resolvePublishedQuestionExam(examId, user);
        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentId(examId, user.getId())
                .orElseThrow(() -> new ErrorMessageException("Экзамен ещё не начался", ErrorCodes.NotFound));
        if (attempt.getStatus() == ExamAttemptStatus.STARTED && isAttemptExpired(attempt)) {
            attempt = finalizeAttempt(attempt);
        }
        return toAttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<StudentExamQuestionResponse> getQuestions(Long examId, User user) {
        resolvePublishedQuestionExam(examId, user);
        ExamAttempt attempt = requireStartedAttempt(examId, user.getId());

        if (attempt.getStatus() != ExamAttemptStatus.STARTED) {
            throw new ErrorMessageException(messageService.get("error.exam.already.submitted"), ErrorCodes.InvalidParams);
        }
        if (isAttemptExpired(attempt)) {
            finalizeAttempt(attempt);
            throw new ErrorMessageException(messageService.get("error.exam.time.expired.auto"), ErrorCodes.InvalidParams);
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
            throw new ErrorMessageException(messageService.get("error.exam.already.submitted"), ErrorCodes.InvalidParams);
        }
        if (isAttemptExpired(attempt)) {
            finalizeAttempt(attempt);
            throw new ErrorMessageException(messageService.get("error.exam.time.expired.auto"), ErrorCodes.InvalidParams);
        }

        if (isExpired(attempt.getExam())) {
            throw new ErrorMessageException(messageService.get("error.exam.deadline.passed"), ErrorCodes.InvalidParams);
        }

        ExamQuestion examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new NotFoundException("Вопрос экзамена не найден: " + examQuestionId));
        if (!examQuestion.getExam().getId().equals(examId)) {
            throw new ErrorMessageException(messageService.get("error.question.not.for.exam"), ErrorCodes.InvalidParams);
        }

        AnswerRequest answerRequest = new AnswerRequest(examQuestionId, request.textAnswer(), request.selectedOptionIds());

        StudentAnswer existing = answerRepository
                .findByStudentIdAndExamQuestionId(user.getId(), examQuestionId)
                .orElse(null);

        StudentAnswer answer;
        if (existing == null) {
            answer = answerMapper.toEntity(answerRequest, examQuestion, user, LocalDateTime.now());
        } else {
            answer = existing;
            answerMapper.updateEntity(answer, answerRequest, examQuestion, LocalDateTime.now());
        }
        applySelectedOptions(answer, request.selectedOptionIds());
        return answerMapper.toResponse(answerRepository.save(answer));
    }

    private void applySelectedOptions(StudentAnswer answer, List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            answerMapper.applySelectedOptions(answer, List.of());
            return;
        }
        List<QuestionOption> options = questionOptionRepository.findAllById(optionIds);
        if (options.size() != optionIds.size()) {
            throw new NotFoundException(messageService.get("error.answer.options.not.found"));
        }
        answerMapper.applySelectedOptions(answer, options);
    }

    @Transactional
    public StudentExamAttemptResponse submitAttempt(Long examId, User user) {
        ExamAttempt attempt = requireStartedAttempt(examId, user.getId());
        if (attempt.getStatus() != ExamAttemptStatus.STARTED) {
            throw new ErrorMessageException(messageService.get("error.exam.already.submitted"), ErrorCodes.AlreadyExists);
        }
        attempt.setSubmittedAt(LocalDateTime.now());

        List<ExamQuestion> questions = examQuestionRepository.findByExamId(examId);
        List<StudentAnswer> answers = answerRepository.findByStudentIdAndExamQuestionExamId(user.getId(), examId);

        // Auto-grade CLOSED and MULTIPLE_CHOICE questions
        for (ExamQuestion eq : questions) {
            QuestionType type = eq.getQuestion().getType();
            if (type == QuestionType.CLOSED || type == QuestionType.MULTIPLE_CHOICE) {
                answers.stream()
                        .filter(a -> a.getExamQuestion().getId().equals(eq.getId()))
                        .findFirst()
                        .ifPresent(a -> {
                            autoGrade(a, eq);
                            answerRepository.save(a);
                        });
            }
        }

        // If no OPEN questions — mark attempt as GRADED immediately
        boolean hasOpenQuestions = questions.stream()
                .anyMatch(eq -> eq.getQuestion().getType() == QuestionType.OPEN);
        attempt.setStatus(hasOpenQuestions ? ExamAttemptStatus.SUBMITTED : ExamAttemptStatus.GRADED);

        return toAttemptResponse(examAttemptRepository.save(attempt));
    }

    private void autoGrade(StudentAnswer answer, ExamQuestion examQuestion) {
        List<Long> correctOptionIds = examQuestion.getQuestion().getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getId)
                .sorted()
                .toList();

        List<Long> selectedIds = answer.getSelectedOptions().stream()
                .map(link -> link.getQuestionOption().getId())
                .sorted()
                .toList();

        boolean isCorrect = correctOptionIds.equals(selectedIds);
        answer.setCorrect(isCorrect);
        answer.setScore(isCorrect ? examQuestion.getScore() : 0);
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
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));
        if (exam.getType() != ExamType.QUESTION) {
            throw new ErrorMessageException(messageService.get("error.exam.not.question.type"), ErrorCodes.InvalidParams);
        }
        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }
        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }
        return exam;
    }

    private ExamAttempt requireStartedAttempt(Long examId, Long studentId) {
        return examAttemptRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ErrorMessageException("Сначала начните экзамен", ErrorCodes.InvalidParams));
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

    private LocalDateTime resolveAttemptDeadline(ExamAttempt attempt) {
        LocalDateTime byTaskLimit = null;
        Integer taskLimit = attempt.getExam().getTaskLimit();
        if (taskLimit != null && taskLimit > 0 && attempt.getStartedAt() != null) {
            byTaskLimit = attempt.getStartedAt().plusMinutes(taskLimit);
        }

        LocalDateTime examEnd = attempt.getExam().getEndAt();
        if (byTaskLimit == null) return examEnd;
        if (examEnd == null) return byTaskLimit;
        return byTaskLimit.isBefore(examEnd) ? byTaskLimit : examEnd;
    }

    private boolean isAttemptExpired(ExamAttempt attempt) {
        LocalDateTime deadline = resolveAttemptDeadline(attempt);
        return deadline != null && !LocalDateTime.now().isBefore(deadline);
    }

    private ExamAttempt finalizeAttempt(ExamAttempt attempt) {
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        if (attempt.getSubmittedAt() == null) {
            attempt.setSubmittedAt(LocalDateTime.now());
        }
        return examAttemptRepository.save(attempt);
    }

    private void ensureStudent(User user) {
        boolean isStudent = user.getRoles().stream().map(Role::getRoleName)
                .anyMatch(roleName -> roleName == RoleName.ROLE_STUDENT);
        if (!isStudent) {
            throw new ErrorMessageException(messageService.get("error.forbidden"), ErrorCodes.Forbidden);
        }
    }

    private StudentExamAttemptResponse toAttemptResponse(ExamAttempt attempt) {
        LocalDateTime deadline = resolveAttemptDeadline(attempt);
        long remainingSeconds = deadline == null ? Long.MAX_VALUE : java.time.Duration.between(LocalDateTime.now(), deadline).getSeconds();

        Integer totalScore = null;
        if (attempt.getStatus() == ExamAttemptStatus.GRADED) {
            totalScore = answerRepository
                    .findByStudentIdAndExamQuestionExamId(attempt.getStudent().getId(), attempt.getExam().getId())
                    .stream()
                    .mapToInt(a -> a.getScore() == null ? 0 : a.getScore())
                    .sum();
        }

        return new StudentExamAttemptResponse(
                attempt.getExam().getId(),
                attempt.getStudent().getId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                deadline,
                Math.max(0L, remainingSeconds),
                totalScore
        );
    }
}
