package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.rspcm.model.enums.PracticeParticipationStatus;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PracticeParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_practice_id")
    private ExamPractice examPractice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readyAt;

    private LocalDateTime chosenAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeParticipationStatus status;
}
