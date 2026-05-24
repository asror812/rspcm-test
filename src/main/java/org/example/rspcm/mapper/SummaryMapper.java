package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.ExamSummary;
import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.QuestionSummary;
import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.entity.*;
import org.springframework.stereotype.Component;

@Component
public class SummaryMapper {

    public UserSummary toUserSummary(User user) {
        return new UserSummary(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    public SubjectSummary toSubjectSummary(Subject subject) {
        return new SubjectSummary(subject.getId(), subject.getName(), subject.getDescription());
    }

    public GroupSummary toGroupSummary(StudyGroup group) {
        return new GroupSummary(group.getId(), group.getName(), group.getLanguage());
    }

    public PracticeSummary toPracticeSummary(Practice practice) {
        return new PracticeSummary(
                practice.getId(),
                practice.getName(),
                toSubjectSummary(practice.getSubject()),
                practice.getWorkMode(),
                practice.getTeamSize(),
                practice.isSchedulingRequired()
        );
    }

    public PracticeSummary toPracticeSummaryWithoutSubject(Practice practice) {
        return new PracticeSummary(
                practice.getId(),
                practice.getName(),
                null,
                practice.getWorkMode(),
                practice.getTeamSize(),
                practice.isSchedulingRequired()
        );
    }

    public ExamSummary toExamSummary(Exam exam) {
        return new ExamSummary(exam.getId(), exam.getTitle(), exam.getEndAt());
    }

    public QuestionSummary toQuestionSummary(Question question) {
        return new QuestionSummary(question.getId(), question.getText(), question.getType());
    }
}
