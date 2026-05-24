package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.rspcm.model.enums.ExamAttemptStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_attempts",
        uniqueConstraints = @UniqueConstraint(name = "uk_exam_attempt_exam_student", columnNames = {"exam_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamAttemptStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;
}
