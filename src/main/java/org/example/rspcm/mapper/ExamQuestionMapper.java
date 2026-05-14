package org.example.rspcm.mapper;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamQuestionMapper {
    private final SummaryMapper summaryMapper;

    public ExamQuestionResponse toResponse(ExamQuestion examQuestion) {
        return new ExamQuestionResponse(
                examQuestion.getId(),
                summaryMapper.toExamSummary(examQuestion.getExam()),
                summaryMapper.toQuestionSummary(examQuestion.getQuestion()),
                examQuestion.getScore(),
                examQuestion.getOrderIndex()
        );
    }

    public ExamQuestion toEntity(ExamQuestionRequest request, Exam exam, Question question) {
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setScore(request.score());
        examQuestion.setOrderIndex(request.orderIndex());
        return examQuestion;
    }

    public void updateEntity(ExamQuestion examQuestion, ExamQuestionRequest request, Exam exam, Question question) {
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setScore(request.score());
        examQuestion.setOrderIndex(request.orderIndex());
    }
}
