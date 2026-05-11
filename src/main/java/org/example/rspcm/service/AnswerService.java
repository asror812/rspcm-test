package org.example.rspcm.service;

import org.example.rspcm.dto.answer.AnswerRequest;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.dto.answer.AnswerScoreRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.QuestionOption;
import org.example.rspcm.model.entity.StudentAnswer;
import org.example.rspcm.model.entity.StudentAnswerOption;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.mapper.AnswerMapper;
import org.example.rspcm.repository.AnswerRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final CurrentUserService currentUserService;

    public List<StudentAnswer> findAll() {
        return answerRepository.findAll();
    }

    public List<AnswerResponse> findAllResponse() {
        return findAll().stream().map(AnswerMapper::toResponse).toList();
    }

    public StudentAnswer findById(Long id) {
        StudentAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Answer topilmadi: " + id));
        validateCanAccess(answer);
        return answer;
    }

    public AnswerResponse findResponseById(Long id) {
        return AnswerMapper.toResponse(findById(id));
    }

    @Transactional
    public StudentAnswer create(AnswerRequest request) {
        ExamQuestion examQuestion = examQuestionRepository.findById(request.examQuestionId())
                .orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + request.examQuestionId()));

        StudentAnswer answer = StudentAnswer.builder()
                .examQuestion(examQuestion)
                .student(currentUserService.getCurrentUser())
                .textAnswer(request.textAnswer())
                .answeredAt(LocalDateTime.now())
                .build();
        applySelectedOptions(answer, request.selectedOptionIds());
        return answerRepository.save(answer);
    }

    public AnswerResponse createResponse(AnswerRequest request) {
        return AnswerMapper.toResponse(create(request));
    }

    @Transactional
    public StudentAnswer update(Long id, AnswerRequest request) {
        StudentAnswer answer = findById(id);
        answer.setExamQuestion(examQuestionRepository.findById(request.examQuestionId())
                .orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + request.examQuestionId())));
        answer.setTextAnswer(request.textAnswer());
        answer.setAnsweredAt(LocalDateTime.now());
        applySelectedOptions(answer, request.selectedOptionIds());
        return answerRepository.save(answer);
    }

    public AnswerResponse updateResponse(Long id, AnswerRequest request) {
        return AnswerMapper.toResponse(update(id, request));
    }

    @Transactional
    public StudentAnswer score(Long id, AnswerScoreRequest request) {
        StudentAnswer answer = findById(id);
        answer.setScore(request.score());
        answer.setCorrect(request.correct());
        return answerRepository.save(answer);
    }

    public AnswerResponse scoreResponse(Long id, AnswerScoreRequest request) {
        return AnswerMapper.toResponse(score(id, request));
    }

    @Transactional
    public void delete(Long id) {
        StudentAnswer answer = findById(id);
        answerRepository.delete(answer);
    }

    private void applySelectedOptions(StudentAnswer answer, List<Long> optionIds) {
        answer.setSelectedOptions(new ArrayList<>());
        if (optionIds == null || optionIds.isEmpty()) {
            return;
        }
        List<QuestionOption> options = questionOptionRepository.findAllById(optionIds);
        for (QuestionOption option : options) {
            StudentAnswerOption selected = new StudentAnswerOption();
            selected.setStudentAnswer(answer);
            selected.setQuestionOption(option);
            answer.getSelectedOptions().add(selected);
        }
    }

    private void validateCanAccess(StudentAnswer answer) {
        User currentUser = currentUserService.getCurrentUser();
        boolean isPrivileged = currentUser.getRoles().stream()
                .map(role -> role.getRoleName())
                .anyMatch(roleName -> roleName == RoleName.ROLE_ADMIN || roleName == RoleName.ROLE_TEACHER);
        if (isPrivileged) {
            return;
        }
        if (!answer.getStudent().getId().equals(currentUser.getId())) {
            throw new ErrorMessageException("Siz bu javobga kira olmaysiz", ErrorCodes.Forbidden);
        }
    }
}
