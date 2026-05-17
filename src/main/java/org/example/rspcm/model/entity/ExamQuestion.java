package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "exam_questions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_exam_question_exam_question", columnNames = {"exam_id", "question_id"}),
                @UniqueConstraint(name = "uk_exam_question_exam_order", columnNames = {"exam_id", "order_index"})
        }
)
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
