package org.example.rspcm.service;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.mapper.ExamQuestionMapper;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionMapper examQuestionMapper;
    private final TeacherProfileRepository teacherProfileRepository;

    public ExamQuestionResponse create(ExamQuestionRequest request) {
        validateRequest(request);
        var exam = validateExamTypeForQuestion(request.examId());

        validateQuestionCapacityAndScoreForCreate(exam, exam.getTaskLimit(), request.score());

        ExamQuestion examQuestion = examQuestionMapper.toEntity(
                request,
                exam,
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId()))
        );

        return examQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    public List<ExamQuestionResponse> findAll(Long subjectId, boolean own, User user, Pageable pageable) {
        Long createdById = own ? user.getId() : null;

        if (isAdmin(user)) {
            return examQuestionRepository.searchAll(subjectId, own, createdById, pageable)
                    .stream().map(examQuestionMapper::toResponse).toList();
        }

        validateTeacherAccess(user.getId(), subjectId);

        return examQuestionRepository.searchAll(subjectId, own, createdById, pageable)
                .stream().map(examQuestionMapper::toResponse).toList();
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

    private void validateTeacherAccess(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException("Fan bo'yicha filtr kiritilishi shart", ErrorCodes.BadRequest);
        }

        boolean teachesSubject = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(userId, subjectId);
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

    private boolean isStudent(User user) {
        return hasRole(user, RoleName.ROLE_STUDENT);
    }
}
