package org.example.rspcm.mapper;

import org.example.rspcm.dto.answer.AnswerRequest;
import org.example.rspcm.dto.answer.AnswerScoreRequest;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.QuestionOption;
import org.example.rspcm.model.entity.StudentAnswer;
import org.example.rspcm.model.entity.StudentAnswerOption;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnswerMapper {
    private final SummaryMapper summaryMapper;

    public AnswerResponse toResponse(StudentAnswer answer) {
        return new AnswerResponse(
                answer.getId(),
                summaryMapper.toQuestionSummary(answer.getExamQuestion().getQuestion()),
                answer.getTextAnswer(),
                answer.getSelectedOptions().stream().map(option -> option.getQuestionOption().getId()).toList(),
                answer.getScore(),
                answer.getCorrect(),
                answer.getAnsweredAt()
        );
    }

    public StudentAnswer toEntity(AnswerRequest request, ExamQuestion examQuestion, User student, LocalDateTime answeredAt) {
        return StudentAnswer.builder()
                .examQuestion(examQuestion)
                .student(student)
                .textAnswer(request.textAnswer())
                .answeredAt(answeredAt)
                .build();
    }

    public void updateEntity(StudentAnswer answer, AnswerRequest request, ExamQuestion examQuestion, LocalDateTime answeredAt) {
        answer.setExamQuestion(examQuestion);
        answer.setTextAnswer(request.textAnswer());
        answer.setAnsweredAt(answeredAt);
    }

    public void applySelectedOptions(StudentAnswer answer, List<QuestionOption> options) {
        answer.setSelectedOptions(new ArrayList<>());
        for (QuestionOption option : options) {
            StudentAnswerOption selected = new StudentAnswerOption();
            selected.setStudentAnswer(answer);
            selected.setQuestionOption(option);
            answer.getSelectedOptions().add(selected);
        }
    }

    public void applyScore(StudentAnswer answer, AnswerScoreRequest request) {
        answer.setScore(request.score());
        answer.setCorrect(request.correct());
    }
}
