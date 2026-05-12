package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_answers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;


    //FOR OPEN QUESTION
    @Column(length = 5000)
    private String textAnswer;

    @Builder.Default
    @OneToMany(
            mappedBy = "studentAnswer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StudentAnswerOption> selectedOptions = new ArrayList<>();

    private Integer score;

    private Boolean correct;

    private LocalDateTime answeredAt;
}
