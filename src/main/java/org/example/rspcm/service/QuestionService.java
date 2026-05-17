package org.example.rspcm.service;

import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.QuestionMapper;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.QuestionRepository;
import org.example.rspcm.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.rspcm.model.enums.RoleName.ROLE_ADMIN;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionMapper questionMapper;
    private final TeacherProfileRepository teacherProfileRepository;

    public Page<QuestionResponse> findAll(Long subjectId, boolean own, User createdBy, Pageable pageable) {
        if (own) {
            return questionRepository.findAllOwnQuestionsBySubjectId(subjectId, createdBy.getId(), pageable)
                    .map(questionMapper::toResponse);
        }
        return questionRepository.findAllBySubjectId(subjectId, pageable).map(questionMapper::toResponse);
    }

    public List<Question> findBySubject(Long subjectId) {
        return questionRepository.findBySubjectIdAndDeletedFalse(subjectId);
    }

    public List<QuestionResponse> findBySubjectResponse(Long subjectId) {
        return questionRepository.findBySubjectIdAndDeletedFalse(subjectId).stream().map(questionMapper::toResponse).toList();
    }

    public List<Question> findOwnCreatedBySubject(User user, Long subjectId) {
        return questionRepository.findByCreatedByIdAndSubjectIdAndDeletedFalse(user.getId(), subjectId);
    }

    public List<QuestionResponse> findOwnCreatedBySubjectResponse(User user, Long subjectId) {
        return questionRepository.findByCreatedByIdAndSubjectIdAndDeletedFalse(user.getId(), subjectId)
                .stream().map(questionMapper::toResponse).toList();
    }

    public Question findById(Long id) {
        return questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Question topilmadi: " + id));
    }

    public QuestionResponse findResponseById(Long id) {
        return questionMapper.toResponse(questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Question topilmadi: " + id)));
    }

    @Transactional
    public Question create(QuestionRequest request, User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(ROLE_ADMIN));

        if (isAdmin) {
            Question question = questionMapper.toEntity(
                    request,
                    subjectRepository.findById(request.subjectId())
                            .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())),
                    user
            );

            question = questionRepository.save(question);
            return question;
        }

        boolean teachesSubject = teacherProfileRepository
                .existsByUserIdAndTeachingSubjectsId(user.getId(), request.subjectId());

        if (!teachesSubject) {
            throw new ErrorMessageException("O'qituvchi faqat o'zi dars beradigan fan uchun savol yarata oladi", ErrorCodes.Forbidden);
        }

        Question question = questionMapper.toEntity(
                request,
                subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())),
                user
        );

        return questionRepository.save(question);
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = findById(id);
        questionMapper.updateEntity(
                question,
                request,
                subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId()))
        );

        return questionMapper.toResponse(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id, User user) {
        Question question = findById(id);

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(ROLE_ADMIN));

        if (isAdmin) {
            question.setDeleted(true);
            questionRepository.save(question);
            return;
        }

        boolean teachesSubject = teacherProfileRepository
                .existsByUserIdAndTeachingSubjectsId(user.getId(), question.getSubject().getId());

        if (!teachesSubject) {
            throw new ErrorMessageException(
                    "Savollarni faqat o'qitayotgan fanlaringiz bo'yicha o'chira olasiz",
                    ErrorCodes.Forbidden
            );
        }

        question.setDeleted(true);
        questionRepository.save(question);
    }

}
