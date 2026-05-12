package org.example.rspcm.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "practical_task_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_student_assignment",
                        columnNames = {"exam_id", "student_id"}
                ),
                @UniqueConstraint(
                        name = "uk_exam_team_assignment",
                        columnNames = {"exam_id", "team_id"}
                ),
                @UniqueConstraint(
                        name = "uk_exam_task_assignment",
                        columnNames = {"exam_id", "practical_task_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticalTaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practical_task_id", nullable = false)
    private PracticalTask practicalTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private PracticeTeam team;

    private LocalDateTime chosenAt;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticalTaskAssignmentStatus status;

    private Integer score;

    @Column(length = 2000)
    private String teacherComment;
}