package org.example.rspcm.service;

import jakarta.validation.constraints.NotNull;
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

    public ExamQuestionResponse create(ExamQuestionRequest request, User user) {
        validateRequest(request);

        var exam = validateExamTypeForQuestion(request.examId());

        checkForDuplicate(exam, request.questionId());
        validateQuestionCapacityAndScoreForCreate(exam, exam.getTaskLimit(), request.score());

        if (isTeacher(user)) {
            validateTeacherAccess(user.getId(), exam.getSubject().getId(), request.examId());
        }

        if (exam.getQuestions().stream()
                .anyMatch(eq -> eq.getOrderIndex().equals(request.orderIndex()))) {
            throw new ErrorMessageException("Bu orderIndex imtihonda allaqachon mavjud", ErrorCodes.BadRequest);
        }

        ExamQuestion examQuestion = examQuestionMapper.toEntity(
                request,
                exam,
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId())),
                user
        );

        return examQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    private void checkForDuplicate(Exam exam, @NotNull Long aLong) {
        boolean exists = examQuestionRepository.existsByExamIdAndQuestionId(exam.getId(), aLong);
        if (exists) {
            throw new ErrorMessageException("Bu imtihonda bu savol allaqachon mavjud", ErrorCodes.BadRequest);
        }
    }

    public Page<ExamQuestionResponse> findAll(Long examId, Long subjectId, boolean own, User user, Pageable pageable) {
        Long createdById = own ? user.getId() : null;

        if (isAdmin(user)) {
            return examQuestionRepository.searchAll(examId, subjectId, createdById, pageable)
                    .map(examQuestionMapper::toResponse);
        }

        validateTeacherAccess(user.getId(), subjectId, examId);

        return examQuestionRepository.searchAll(examId, subjectId, createdById, pageable)
                .map(examQuestionMapper::toResponse);
    }

    public ExamQuestion findById(Long id) {
        return examQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + id));
    }

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
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId()))
        );
        return examQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    @Transactional
    public void delete(Long id) {
        examQuestionRepository.delete(findById(id));
    }

    private void validateRequest(ExamQuestionRequest request) {
        if (request.score() <= 0) {
            throw new ErrorMessageException("Score 0 dan katta bo'lishi kerak", ErrorCodes.BadRequest);
        }
        if (request.orderIndex() <= 0) {
            throw new ErrorMessageException("orderIndex 0 dan katta bo'lishi kerak", ErrorCodes.BadRequest);
        }
    }

    private Exam validateExamTypeForQuestion(Long examId) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));

        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new ErrorMessageException("Examni faqat Draft statusida yangilab buladi", ErrorCodes.BadRequest);
        }

        if (exam.getType() != ExamType.QUESTION) {
            throw new ErrorMessageException("Faqat QUESTION turidagi imtihonga savol qo'shish mumkin", ErrorCodes.BadRequest);
        }

        return exam;
    }

    private void validateQuestionCapacityAndScoreForCreate(Exam exam, Integer limit, Integer newScore) {
        long currentCount = examQuestionRepository.countByExamId(exam.getId());
        if (limit != null && currentCount >= limit) {
            throw new ErrorMessageException("Bu imtihonda savollar soni yetarli", ErrorCodes.BadRequest);
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
            throw new ErrorMessageException("Bu imtihonda savollar soni yetarli", ErrorCodes.BadRequest);
        }
    }

    private void validateTeacherAccess(Long userId, Long subjectId, Long examId) {
        if (subjectId == null && examId == null) {
            throw new ErrorMessageException("Filtr uchun subjectId yoki examId kiritilishi shart", ErrorCodes.BadRequest);
        }

        Long resolvedSubjectId = subjectId;
        if (resolvedSubjectId == null) {
            var exam = examRepository.findById(examId)
                    .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));
            if (exam.getSubject() == null) {
                throw new ErrorMessageException("Bu exam uchun subject biriktirilmagan", ErrorCodes.BadRequest);
            }
            resolvedSubjectId = exam.getSubject().getId();
        }

        boolean teachesSubject = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(userId, resolvedSubjectId);
        if (!teachesSubject) {
            throw new ErrorMessageException("Faqat o'zingizga biriktirilgan fan imtihonlarini ko'ra olasiz", ErrorCodes.Forbidden);
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
