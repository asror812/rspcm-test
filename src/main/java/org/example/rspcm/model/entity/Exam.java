package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer maxScore;

    @Column(nullable = false)
    private Integer taskLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    @OrderBy("orderIndex asc")
    private List<ExamQuestion> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex asc")
    private List<ExamPractice> practices = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exam_groups",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<StudyGroup> groups = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exam_students",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> targetStudents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public void addPractice(Practice practice, Integer score, Integer orderIndex, LocalDateTime deadline) {
        ExamPractice examPractice = ExamPractice.builder()
                .exam(this)
                .practice(practice)
                .score(score)
                .orderIndex(orderIndex)
                .deadline(deadline)
                .build();

        this.practices.add(examPractice);
    }

    public void removePractice(ExamPractice examPractice) {
        this.practices.remove(examPractice);
        examPractice.setExam(null);
    }

    public void addQuestion(Practice practice, Integer score, Integer orderIndex, LocalDateTime deadline) {
        ExamPractice examPractice = ExamPractice.builder()
                .exam(this)
                .practice(practice)
                .score(score)
                .orderIndex(orderIndex)
                .deadline(deadline)
                .build();

        this.practices.add(examPractice);
    }

    public void removeQuestion(ExamQuestion examQuestion) {
        this.questions.remove(examQuestion);
        examQuestion.setExam(null);
    }
}
