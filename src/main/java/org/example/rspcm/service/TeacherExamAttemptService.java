package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.teacher.TeacherAnswerOptionItem;
import org.example.rspcm.dto.exam.teacher.TeacherAttemptAnswerItem;
import org.example.rspcm.dto.exam.teacher.TeacherAttemptSummaryResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.ExamAttempt;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.StudentAnswer;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamAttemptStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.QuestionType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.AnswerRepository;
import org.example.rspcm.repository.ExamAttemptRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherExamAttemptService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final AnswerRepository answerRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SummaryMapper summaryMapper;
    private final MessageService messageService;

    @Transactional(readOnly = true)
    public List<TeacherAttemptSummaryResponse> getAttemptsByExam(Long examId, User user) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));

        if (exam.getType() != ExamType.QUESTION) {
            throw new ErrorMessageException(messageService.get("error.attempt.question.only"), ErrorCodes.BadRequest);
        }
        validateAccess(user, exam.getSubject() == null ? null : exam.getSubject().getId(),
                exam.getCreatedBy() == null ? null : exam.getCreatedBy().getId());

        List<ExamQuestion> openQuestions = examQuestionRepository.findByExamId(examId)
                .stream()
                .filter(eq -> eq.getQuestion().getType() == QuestionType.OPEN)
                .toList();

        return examAttemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getStatus() == ExamAttemptStatus.SUBMITTED
                        || a.getStatus() == ExamAttemptStatus.GRADED)
                .map(attempt -> toSummary(attempt, openQuestions))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherAttemptAnswerItem> getAttemptAnswers(Long examId, Long attemptId, User user) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));

        validateAccess(user, exam.getSubject() == null ? null : exam.getSubject().getId(),
                exam.getCreatedBy() == null ? null : exam.getCreatedBy().getId());

        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.attempt.not.found", attemptId)));

        if (!attempt.getExam().getId().equals(examId)) {
            throw new NotFoundException(messageService.get("error.attempt.not.found", attemptId));
        }

        List<StudentAnswer> answers = answerRepository
                .findByStudentIdAndExamQuestionExamId(attempt.getStudent().getId(), examId);

        return examQuestionRepository.findByExamId(examId).stream()
                .sorted(Comparator.comparing(ExamQuestion::getOrderIndex))
                .map(eq -> toAnswerItem(eq, answers))
                .toList();
    }

    private TeacherAttemptSummaryResponse toSummary(ExamAttempt attempt, List<ExamQuestion> openQuestions) {
        Long examId = attempt.getExam().getId();
        List<StudentAnswer> answers = answerRepository
                .findByStudentIdAndExamQuestionExamId(attempt.getStudent().getId(), examId);

        Integer totalScore = attempt.getStatus() == ExamAttemptStatus.GRADED
                ? answers.stream().mapToInt(a -> a.getScore() == null ? 0 : a.getScore()).sum()
                : null;

        int ungradedOpen = (int) openQuestions.stream()
                .filter(eq -> answers.stream()
                        .noneMatch(a -> a.getExamQuestion().getId().equals(eq.getId())
                                && a.getScore() != null))
                .count();

        return new TeacherAttemptSummaryResponse(
                attempt.getId(),
                summaryMapper.toUserSummary(attempt.getStudent()),
                attempt.getStatus(),
                attempt.getSubmittedAt(),
                totalScore,
                ungradedOpen
        );
    }

    private TeacherAttemptAnswerItem toAnswerItem(ExamQuestion eq, List<StudentAnswer> answers) {
        StudentAnswer answer = answers.stream()
                .filter(a -> a.getExamQuestion().getId().equals(eq.getId()))
                .findFirst().orElse(null);

        List<TeacherAnswerOptionItem> options = eq.getQuestion().getOptions() == null
                ? List.of()
                : eq.getQuestion().getOptions().stream()
                        .sorted(Comparator.comparingInt(o -> o.getOrderIndex() == null ? 0 : o.getOrderIndex()))
                        .map(opt -> new TeacherAnswerOptionItem(
                                opt.getId(), opt.getText(), opt.isCorrect(),
                                opt.getOrderIndex() == null ? 0 : opt.getOrderIndex()))
                        .toList();

        List<Long> selectedOptionIds = answer == null ? List.of()
                : answer.getSelectedOptions().stream()
                        .map(link -> link.getQuestionOption().getId())
                        .toList();

        return new TeacherAttemptAnswerItem(
                answer == null ? null : answer.getId(),
                eq.getId(),
                eq.getOrderIndex(),
                eq.getQuestion().getText(),
                eq.getQuestion().getType(),
                eq.getScore(),
                answer == null ? null : answer.getTextAnswer(),
                selectedOptionIds,
                options,
                answer == null ? null : answer.getScore(),
                answer == null ? null : answer.getCorrect()
        );
    }

    private void validateAccess(User user, Long subjectId, Long createdById) {
        if (isAdmin(user)) return;
        if (!isTeacher(user)) {
            throw new ErrorMessageException(messageService.get("error.no.access"), ErrorCodes.Forbidden);
        }
        // Exam creator always has access
        if (createdById != null && createdById.equals(user.getId())) return;
        // Teacher assigned to the subject has access
        if (subjectId != null && teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), subjectId)) return;
        throw new ErrorMessageException(messageService.get("error.no.access.exam"), ErrorCodes.Forbidden);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleName.ROLE_TEACHER);
    }
}
