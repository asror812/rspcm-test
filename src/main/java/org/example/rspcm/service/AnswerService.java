package org.example.rspcm.service;

import org.example.rspcm.dto.answer.AnswerRequest;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.dto.answer.AnswerScoreRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.*;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.mapper.AnswerMapper;
import org.example.rspcm.repository.AnswerRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AnswerMapper answerMapper;

    public List<AnswerResponse> findAll() {
        return answerRepository.findAll().stream().map(answerMapper::toResponse).toList();
    }

    public List<AnswerResponse> findMineResponse() {
        User currentUser = currentUser();
        return answerRepository.findByStudentId(currentUser.getId()).stream()
                .map(answerMapper::toResponse)
                .toList();
    }

    public StudentAnswer findById(Long id) {
        StudentAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ответ не найден: " + id));
        validateCanAccess(answer);
        return answer;
    }

    public AnswerResponse findResponseById(Long id) {
        StudentAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ответ не найден: " + id));
        validateCanAccess(answer);
        return answerMapper.toResponse(answer);
    }

    public AnswerResponse createResponse(AnswerRequest request) {
        User user = currentUser();
        ExamQuestion examQuestion = examQuestionRepository.findById(request.examQuestionId())
                .orElseThrow(() -> new NotFoundException("Вопрос экзамена не найден: " + request.examQuestionId()));
        validateStudentQuestionExamWrite(examQuestion, user);
        StudentAnswer answer = answerMapper.toEntity(
                request,
                examQuestion,
                user,
                LocalDateTime.now()
        );
        applySelectedOptions(answer, request.selectedOptionIds());
        return answerMapper.toResponse(answerRepository.save(answer));
    }

    @Transactional
    public AnswerResponse update(Long id, AnswerRequest request) {
        StudentAnswer answer = findById(id);
        User user = currentUser();
        validateStudentQuestionExamWrite(answer.getExamQuestion(), user);
        answerMapper.updateEntity(
                answer,
                request,
                examQuestionRepository.findById(request.examQuestionId())
                        .orElseThrow(() -> new NotFoundException("Вопрос экзамена не найден: " + request.examQuestionId())),
                LocalDateTime.now()
        );
        applySelectedOptions(answer, request.selectedOptionIds());
        return answerMapper.toResponse(answerRepository.save(answer));
    }

    public AnswerResponse scoreResponse(Long id, AnswerScoreRequest request) {
        StudentAnswer answer = findById(id);
        answerMapper.applyScore(answer, request);
        return answerMapper.toResponse(answerRepository.save(answer));
    }

    @Transactional
    public void delete(Long id) {
        StudentAnswer answer = findById(id);
        answerRepository.delete(answer);
    }

    private void applySelectedOptions(StudentAnswer answer, List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            answerMapper.applySelectedOptions(answer, List.of());
            return;
        }
        List<QuestionOption> options = questionOptionRepository.findAllById(optionIds);
        if (options.size() != optionIds.size()) {
            throw new NotFoundException("Некоторые варианты ответа не найдены");
        }
        answerMapper.applySelectedOptions(answer, options);
    }

    private void validateCanAccess(StudentAnswer answer) {
        User currentUser = currentUser();
        boolean isPrivileged = currentUser.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(roleName -> roleName == RoleName.ROLE_ADMIN || roleName == RoleName.ROLE_TEACHER);
        if (isPrivileged) {
            return;
        }
        if (!answer.getStudent().getId().equals(currentUser.getId())) {
            throw new ErrorMessageException("У вас нет доступа к этому ответу", ErrorCodes.Forbidden);
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private void validateStudentQuestionExamWrite(ExamQuestion examQuestion, User user) {
        boolean isStudent = user.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(roleName -> roleName == RoleName.ROLE_STUDENT);
        if (isStudent && examQuestion.getExam().getType() == ExamType.QUESTION) {
            throw new ErrorMessageException(
                    "Для ответов на экзамен типа QUESTION используйте endpoint /api/exams/{examId}/questions/{examQuestionId}/answer",
                    ErrorCodes.Forbidden
            );
        }
    }
}
