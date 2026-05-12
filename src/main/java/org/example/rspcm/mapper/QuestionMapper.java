package org.example.rspcm.mapper;

import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.dto.question.QuestionOptionResponse;
import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.QuestionOption;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;

import java.util.ArrayList;
import java.util.List;

public final class QuestionMapper {
    private QuestionMapper() {
    }

    public static QuestionResponse toResponse(Question question) {
        List<QuestionOptionResponse> options = question.getOptions() == null ? List.of() : question.getOptions().stream()
                .map(QuestionMapper::toOptionResponse)
                .toList();
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getType(),
                SummaryMapper.toSubjectSummary(question.getSubject()),
                SummaryMapper.toUserSummary(question.getCreatedBy()),
                options
        );
    }

    public static QuestionOptionResponse toOptionResponse(QuestionOption option) {
        return new QuestionOptionResponse(option.getId(), option.getText(), option.isCorrect(), option.getOrderIndex());
    }

    public static Question toEntity(QuestionRequest request, Subject subject, User createdBy) {
        Question question = Question.builder()
                .text(request.text())
                .type(request.type())
                .subject(subject)
                .createdBy(createdBy)
                .options(new ArrayList<>())
                .build();
        applyOptions(question, request);
        return question;
    }

    public static void updateEntity(Question question, QuestionRequest request, Subject subject) {
        question.setText(request.text());
        question.setType(request.type());
        question.setSubject(subject);
        applyOptions(question, request);
    }

    public static void applyOptions(Question question, QuestionRequest request) {
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
