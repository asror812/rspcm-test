package org.example.rspcm.service;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.mapper.ExamQuestionMapper;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public List<ExamQuestionResponse> findAll() {
        return examQuestionRepository.findAll().stream().map(ExamQuestionMapper::toResponse).toList();
    }

    public ExamQuestion findById(Long id) {
        return examQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + id));
    }

    public ExamQuestionResponse findResponseById(Long id) {
        return ExamQuestionMapper.toResponse(examQuestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExamQuestion topilmadi: " + id)));
    }

    @Transactional
    public ExamQuestion create(ExamQuestionRequest request) {
        validateRequest(request);
        ExamQuestion examQuestion = ExamQuestionMapper.toEntity(
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId()))
        );
        return examQuestionRepository.save(examQuestion);
    }

    public ExamQuestionResponse createResponse(ExamQuestionRequest request) {
        validateRequest(request);
        ExamQuestion examQuestion = ExamQuestionMapper.toEntity(
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId()))
        );
        return ExamQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
    }

    @Transactional
    public ExamQuestionResponse update(Long id, ExamQuestionRequest request) {
        validateRequest(request);
        ExamQuestion examQuestion = findById(id);
        ExamQuestionMapper.updateEntity(
                examQuestion,
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
                questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new NotFoundException("Question topilmadi: " + request.questionId()))
        );
        return ExamQuestionMapper.toResponse(examQuestionRepository.save(examQuestion));
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
}
