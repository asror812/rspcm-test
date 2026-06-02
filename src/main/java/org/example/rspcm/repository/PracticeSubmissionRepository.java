package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeSubmissionRepository extends JpaRepository<PracticeSubmission, Long> {
    @EntityGraph(attributePaths = {"examParticipation", "examParticipation.exam", "examParticipation.examPractice", "examParticipation.examPractice.practice", "student"})
    Optional<PracticeSubmission> findByExamParticipationId(Long examParticipationId);

    @EntityGraph(attributePaths = {"examParticipation", "examParticipation.exam", "examParticipation.examPractice", "examParticipation.examPractice.practice", "student"})
    Page<PracticeSubmission> findByExamParticipationExamId(Long examId, Pageable pageable);

    @EntityGraph(attributePaths = {"examParticipation", "examParticipation.exam", "examParticipation.examPractice", "examParticipation.examPractice.practice", "student"})
    Page<PracticeSubmission> findByExamParticipationExamIdAndStatus(Long examId, PracticeSubmissionStatus status, Pageable pageable);

    long countByStatus(PracticeSubmissionStatus status);

    Optional<PracticeSubmission> findByExamParticipationExamIdAndStudentId(Long examId, Long studentId);

    @EntityGraph(attributePaths = {"examParticipation", "examParticipation.exam", "examParticipation.examPractice", "examParticipation.examPractice.practice", "student"})
    Page<PracticeSubmission> findBySubmittedAtIsNotNullOrderBySubmittedAtDesc(Pageable pageable);
}
