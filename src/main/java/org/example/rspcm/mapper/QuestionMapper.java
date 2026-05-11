package org.example.rspcm.mapper;

import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.dto.question.QuestionOptionResponse;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.QuestionOption;

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
}
