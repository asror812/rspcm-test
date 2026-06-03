package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_submission_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeSubmissionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private PracticeSubmission submission;

    @Column(length = 5000)
    private String textAnswer;

    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private int attemptNumber;

    @Column(length = 2000)
    private String teacherComment;
}
