package org.example.rspcm.service;

import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.QuestionMapper;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.QuestionOption;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserService currentUserService;

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public List<QuestionResponse> findAllResponse() {
        return findAll().stream().map(QuestionMapper::toResponse).toList();
    }

    public List<Question> findBySubject(Long subjectId) {
        return questionRepository.findBySubjectId(subjectId);
    }

    public List<QuestionResponse> findBySubjectResponse(Long subjectId) {
        return findBySubject(subjectId).stream().map(QuestionMapper::toResponse).toList();
    }

    public List<Question> findOwnCreatedBySubject(Long subjectId) {
        return questionRepository.findByCreatedByIdAndSubjectId(currentUserService.getCurrentUser().getId(), subjectId);
    }

    public List<QuestionResponse> findOwnCreatedBySubjectResponse(Long subjectId) {
        return findOwnCreatedBySubject(subjectId).stream().map(QuestionMapper::toResponse).toList();
    }

    public Question findById(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Question topilmadi: " + id));
    }

    public QuestionResponse findResponseById(Long id) {
        return QuestionMapper.toResponse(findById(id));
    }

    @Transactional
    public Question create(QuestionRequest request) {
        Question question = Question.builder()
                .text(request.text())
                .type(request.type())
                .subject(subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())))
                .createdBy(currentUserService.getCurrentUser())
                .options(new ArrayList<>())
                .build();
        applyOptions(question, request);

        return questionRepository.save(question);
    }

    public QuestionResponse createResponse(QuestionRequest request) {
        return QuestionMapper.toResponse(create(request));
    }

    @Transactional
    public Question update(Long id, QuestionRequest request) {
        Question question = findById(id);
        question.setText(request.text());
        question.setType(request.type());
        question.setSubject(subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())));
        applyOptions(question, request);
        return questionRepository.save(question);
    }

    public QuestionResponse updateResponse(Long id, QuestionRequest request) {
        return QuestionMapper.toResponse(update(id, request));
    }

    @Transactional
    public void delete(Long id) {
        Question question = findById(id);
        questionRepository.delete(question);
    }

    private void applyOptions(Question question, QuestionRequest request) {
        question.getOptions().clear();
        if (request.options() == null || request.options().isEmpty()) {
            return;
        }
        int i = 0;
        for (var optionRequest : request.options()) {
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setText(optionRequest.text());
            option.setCorrect(optionRequest.correct());
            option.setOrderIndex(optionRequest.orderIndex() == null ? i : optionRequest.orderIndex());
            question.getOptions().add(option);
            i++;
        }
    }
}
