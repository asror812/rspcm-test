package org.example.rspcm.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "practice_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_student_assignment",
                        columnNames = {"exam_id", "student_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private PracticeParticipation examParticipation;

    @Column(length = 5000)
    private String textAnswer;

    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeSubmissionStatus status;

    @Column(length = 2000)
    private String teacherComment;
}
