package org.example.rspcm.service;

import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.QuestionMapper;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserService currentUserService;
    private final QuestionMapper questionMapper;

    public List<QuestionResponse> findAll() {
        return questionRepository.findAll().stream().map(questionMapper::toResponse).toList();
    }

    public List<Question> findBySubject(Long subjectId) {
        return questionRepository.findBySubjectId(subjectId);
    }

    public List<QuestionResponse> findBySubjectResponse(Long subjectId) {
        return questionRepository.findBySubjectId(subjectId).stream().map(questionMapper::toResponse).toList();
    }

    public List<Question> findOwnCreatedBySubject(Long subjectId) {
        return questionRepository.findByCreatedByIdAndSubjectId(currentUserService.getCurrentUser().getId(), subjectId);
    }

    public List<QuestionResponse> findOwnCreatedBySubjectResponse(Long subjectId) {
        return questionRepository.findByCreatedByIdAndSubjectId(currentUserService.getCurrentUser().getId(), subjectId)
                .stream().map(questionMapper::toResponse).toList();
    }

    public Question findById(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Question topilmadi: " + id));
    }

    public QuestionResponse findResponseById(Long id) {
        return questionMapper.toResponse(questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question topilmadi: " + id)));
    }

    @Transactional
    public Question create(QuestionRequest request) {
        Question question = questionMapper.toEntity(
                request,
                subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())),
                currentUserService.getCurrentUser()
        );
        return questionRepository.save(question);
    }

    public QuestionResponse createResponse(QuestionRequest request) {
        Question question = questionMapper.toEntity(
                request,
                subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())),
                currentUserService.getCurrentUser()
        );
        return questionMapper.toResponse(questionRepository.save(question));
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
    public void delete(Long id) {
        Question question = findById(id);
        questionRepository.delete(question);
    }

}
