package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.student.StudentDashboardResponse;
import org.example.rspcm.dto.student.StudentTaskItem;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.mapper.SubjectMapper;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentProfileRepository studentProfileRepository;
    private final SubjectMapper subjectMapper;
    private final PracticeParticipationService practiceParticipationService;
    private final ExamRepository examRepository;

    public StudentDashboardResponse getMe(User user) {
        List<SubjectResponse> subjects = studentProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getGroup() == null
                        ? List.<SubjectResponse>of()
                        : profile.getGroup().getSubjects().stream().map(subjectMapper::toResponse).toList())
                .orElse(List.of());

        List<StudentTaskItem> practices = practiceParticipationService.getMyParticipations(user).stream()
                .map(participation -> {
                    var exam = examRepository.findById(participation.examId()).orElse(null);
                    return new StudentTaskItem(
                            participation.practice().id(),
                            participation.practice().name(),
                            exam == null ? null : exam.getEndAt(),
                            "PRACTICE"
                    );
                })
                .sorted(Comparator.comparing(StudentTaskItem::deadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<StudentTaskItem> exams = examRepository
                .findStudentExams(user.getId(), null, null, null, PageRequest.of(0, 20))
                .stream()
                .map(exam -> new StudentTaskItem(exam.getId(), exam.getTitle(), exam.getEndAt(), ExamType.PRACTICE == exam.getType() ? "PRACTICE_EXAM" : "QUESTION_EXAM"))
                .sorted(Comparator.comparing(StudentTaskItem::deadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return new StudentDashboardResponse(subjects, practices, exams);
    }
}
