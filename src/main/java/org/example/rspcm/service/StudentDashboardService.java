package org.example.rspcm.service;

import org.example.rspcm.dto.student.StudentTaskItem;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final CurrentUserService currentUserService;
    private final SubjectRepository subjectRepository;
    private final PracticeRepository practiceRepository;
    private final ExamRepository examRepository;

/*    public StudentDashboardResponse getMyDashboard() {
        User student = currentUserService.getCurrentUser();

        return new StudentDashboardResponse(
                subjectRepository.findDistinctByGroupsStudentsId(student.getId()).stream().map(subjectMapper::toResponse).toList(),
                practiceRepository.findDistinctByGroupsStudentsIdOrTargetStudentsId(student.getId(), student.getId()).stream()
                        .map(this::toPracticeTask)
                        .toList(),
                examRepository.findDistinctByGroupsStudentsIdOrTargetStudentsId(student.getId(), student.getId()).stream()
                        .map(this::toExamTask)
                        .toList()
        );
    }*/

    private StudentTaskItem toPracticeTask(PracticalTask practicalTask) {
        return new StudentTaskItem(practicalTask.getId(), practicalTask.getName(), practicalTask.getDeadline(), "PRACTICE");
    }

    private StudentTaskItem toExamTask(Exam exam) {
        return new StudentTaskItem(exam.getId(), exam.getTitle(), exam.getEndAt(), "EXAM");
    }
}
