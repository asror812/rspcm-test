package org.example.rspcm.mapper;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.dto.question.QuestionOptionResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExamQuestionMapper {
    public ExamQuestionResponse toResponse(ExamQuestion examQuestion) {
        List<QuestionOptionResponse> options = examQuestion.getQuestion().getOptions() == null
                ? List.of()
                : examQuestion.getQuestion().getOptions().stream()
                .map(option -> new QuestionOptionResponse(
                        option.getId(),
                        option.getText(),
                        option.isCorrect(),
                        option.getOrderIndex()
                ))
                .toList();

        return new ExamQuestionResponse(
                examQuestion.getId(),
                examQuestion.getExam().getId(),
                examQuestion.getQuestion().getId(),
                examQuestion.getQuestion().getText(),
                examQuestion.getQuestion().getType(),
                examQuestion.getQuestion().getSubject().getId(),
                examQuestion.getQuestion().getSubject().getName(),
                examQuestion.getScore(),
                examQuestion.getOrderIndex(),
                options
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
