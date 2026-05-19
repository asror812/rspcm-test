package org.example.rspcm.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "student_practice_choices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_exam_choice",
                        columnNames = {"exam_id", "student_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPracticeChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_practice_id", nullable = false)
    private ExamPractice examPractice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
}
