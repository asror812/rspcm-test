package org.example.rspcm.service;

import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.QuestionMapper;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.RoleName;
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

    @Transactional
    public Question create(QuestionRequest request, User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(ROLE_ADMIN));

        if (isAdmin) {
            Question question = questionMapper.toEntity(
                    request,
                    subjectRepository.findById(request.subjectId())
                            .orElseThrow(() -> new NotFoundException("Предмет не найден: " + request.subjectId())),
                    user
            );

            question = questionRepository.save(question);
            return question;
        }

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new NotFoundException("Предмет не найден: " + request.subjectId()));

        boolean teachesSubject = teacherProfileRepository
                .existsByUserIdAndTeachingSubjectsId(user.getId(), request.subjectId());

        if (!teachesSubject) {
            throw new ErrorMessageException("Преподаватель может создавать вопросы только по предметам, которые он ведёт", ErrorCodes.Forbidden);
        }

        Question question = questionMapper.toEntity(
                request,
                subject,
                user
        );

        return questionRepository.save(question);
    }

    public Page<QuestionResponse> findAll(Long subjectId, boolean own, User createdBy, Pageable pageable) {
        Long createdById = own ? createdBy.getId() : null;

        return questionRepository.searchAll(subjectId, createdById, pageable)
                .map(questionMapper::toResponse);
    }

    public Question findById(Long id) {
        return questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден: " + id));
    }

    public QuestionResponse findResponseById(Long id) {
        return questionMapper.toResponse(findById(id));
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request, User user) {
        Question question = findById(id);

        if (isAdmin(user)) {
            questionMapper.updateEntity(
                    question,
                    request,
                    subjectRepository.findById(request.subjectId())
                            .orElseThrow(() -> new NotFoundException("Предмет не найден: " + request.subjectId()))
            );
        }

        boolean teachesSubject = teacherProfileRepository
                .existsByUserIdAndTeachingSubjectsId(user.getId(), request.subjectId());

        if (!teachesSubject) {
            throw new ErrorMessageException("Преподаватель может создавать вопросы только по предметам, которые он ведёт", ErrorCodes.Forbidden);
        }

        questionMapper.updateEntity(
                question,
                request,
                subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Предмет не найден: " + request.subjectId()))
        );

        return questionMapper.toResponse(questionRepository.save(question));
    }

    private void validateTeacherSubjectAccess(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException("Необходимо указать фильтр по предмету", ErrorCodes.BadRequest);
        }

        boolean teachesSubject = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(userId, subjectId);
        if (!teachesSubject) {
            throw new ErrorMessageException("Вы можете просматривать только экзамены по закреплённым за вами предметам", ErrorCodes.Forbidden);
        }
    }

    @Transactional
    public void delete(Long id, User user) {
        Question question = findById(id);

        if (isAdmin(user)) {
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
