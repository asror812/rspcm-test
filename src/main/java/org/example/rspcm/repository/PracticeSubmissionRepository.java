package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeSubmissionRepository extends JpaRepository<PracticeSubmission, Long> {
    Optional<PracticeSubmission> findByExamParticipationId(Long examParticipationId);

    Page<PracticeSubmission> findByExamParticipationExamId(Long examId, Pageable pageable);

    Page<PracticeSubmission> findByExamParticipationExamIdAndStatus(Long examId, PracticeSubmissionStatus status, Pageable pageable);
}
