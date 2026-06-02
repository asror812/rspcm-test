package org.example.rspcm.service;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.mapper.ExamQuestionMapper;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionMapper examQuestionMapper;
    private final TeacherProfileRepository teacherProfileRepository;
    private final MessageService messageService;

    public ExamQuestionResponse create(ExamQuestionRequest request, User user) {
        validateRequest(request);

        var exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", request.examId())));

        checkForDuplicate(exam, request.questionId());
        validateQuestionCapacityAndScoreForCreate(exam, request.score());

        validateTeacherAccess(user, exam);

        if (exam.getQuestions().stream()
                .anyMatch(eq -> eq.getOrderIndex().equals(request.orderIndex()))) {
            throw new ErrorMessageException(messageService.get("error.order.index.exists"), ErrorCodes.BadRequest);
        }

        ExamQuestion examQuestion = examQuestionMapper.toEntity(
                request,
                exam,
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Вопрос не найден: " + request.questionId())),
                user
        );

        return examQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    @Transactional(readOnly = true)
    public Page<ExamQuestionResponse> findAll(Long examId, Long subjectId, boolean own, User user, Pageable pageable) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));

        validateTeacherAccess(user, exam);

        return examQuestionRepository.searchAll(examId, subjectId, own, user.getId(), pageable)
                .map(examQuestionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ExamQuestion findById(Long id) {
        return examQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException("Вопрос экзамена не найден: " + id));
    }

    @Transactional(readOnly = true)
    public ExamQuestionResponse findResponseById(Long id) {
        return examQuestionMapper.toResponse(findById(id));
    }

    @Transactional
    public ExamQuestionResponse update(Long id, ExamQuestionRequest request) {
        validateRequest(request);
        var exam = validateExamTypeForQuestion(request.examId());

        validateQuestionCapacityForUpdate(exam.getId(), exam.getTaskLimit(), id);
        ExamQuestion examQuestion = findById(id);

        examQuestionMapper.updateEntity(
                examQuestion,
                request,
                exam,
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Вопрос не найден: " + request.questionId()))
        );
        return examQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    @Transactional
    public void delete(Long id, User user) {
        ExamQuestion examQuestion = examQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", id)));

        validateTeacherAccess(user, examQuestion.getExam());
        examQuestionRepository.delete(findById(id));
    }

    private void validateRequest(ExamQuestionRequest request) {
        if (request.orderIndex() <= 0) {
            throw new ErrorMessageException(messageService.get("error.order.index.positive"), ErrorCodes.BadRequest);
        }
    }

    private Exam validateExamTypeForQuestion(Long examId) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));

        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new ErrorMessageException(messageService.get("error.exam.not.draft"), ErrorCodes.BadRequest);
        }

        if (exam.getType() != ExamType.QUESTION) {
            throw new ErrorMessageException(messageService.get("error.question.exam.type"), ErrorCodes.BadRequest);
        }

        return exam;
    }

    private void checkForDuplicate(Exam exam, Long questionId) {
        boolean exists = examQuestionRepository.existsByExamIdAndQuestionId(exam.getId(), questionId);
        if (exists) {
            throw new ErrorMessageException(messageService.get("error.question.already.in.exam"), ErrorCodes.BadRequest);
        }
    }

    private void validateQuestionCapacityAndScoreForCreate(Exam exam, Integer newScore) {
        long currentCount = examQuestionRepository.countByExamId(exam.getId());

        if (exam.getTaskLimit() != null && currentCount >= exam.getTaskLimit()) {
            throw new ErrorMessageException(messageService.get("error.exam.enough.questions"), ErrorCodes.BadRequest);
        }

        Integer currentTotalScore = examQuestionRepository.sumScoreByExamId(exam.getId());
        if (exam.getMaxScore() != null && currentTotalScore + newScore > exam.getMaxScore()) {
            throw new ErrorMessageException(
                    "Savollar umumiy bali imtihon maksimal balidan oshib ketmasligi kerak",
                    ErrorCodes.BadRequest
            );
        }
    }

    private void validateQuestionCapacityForUpdate(Long examId, Integer limit, Long examQuestionId) {
        long currentCount = examQuestionRepository.countByExamIdAndIdNot(examId, examQuestionId);
        if (limit != null && currentCount >= limit) {
            throw new ErrorMessageException(messageService.get("error.exam.enough.questions"), ErrorCodes.BadRequest);
        }
    }

    private void validateTeacherAccess(User user, Exam exam) {
        if (isAdmin(user)) return;

        if (!isTeacher(user))
            throw new ErrorMessageException(messageService.get("error.forbidden"), ErrorCodes.Forbidden);

        if (exam.getSubject() == null) {
            throw new ErrorMessageException(messageService.get("error.exam.no.subject"), ErrorCodes.BadRequest);
        }

        boolean teaches = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), exam.getSubject().getId());
        if (!teaches) {
            throw new ErrorMessageException(messageService.get("error.exam.access.denied"), ErrorCodes.Forbidden);
        }
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

}
