package org.example.rspcm.mapper;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.Question;

public final class ExamQuestionMapper {
    private ExamQuestionMapper() {
    }

    public static ExamQuestionResponse toResponse(ExamQuestion examQuestion) {
        return new ExamQuestionResponse(
                examQuestion.getId(),
                SummaryMapper.toExamSummary(examQuestion.getExam()),
                SummaryMapper.toQuestionSummary(examQuestion.getQuestion()),
                examQuestion.getScore(),
                examQuestion.getOrderIndex()
        );
    }

    public static ExamQuestion toEntity(ExamQuestionRequest request, Exam exam, Question question) {
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setScore(request.score());
        examQuestion.setOrderIndex(request.orderIndex());
        return examQuestion;
    }

    public static void updateEntity(ExamQuestion examQuestion, ExamQuestionRequest request, Exam exam, Question question) {
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setScore(request.score());
        examQuestion.setOrderIndex(request.orderIndex());
    }
}
