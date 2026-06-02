package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeSubmissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeSubmissionAttemptRepository extends JpaRepository<PracticeSubmissionAttempt, Long> {
    List<PracticeSubmissionAttempt> findBySubmissionIdOrderByAttemptNumberAsc(Long submissionId);
    int countBySubmissionId(Long submissionId);
}
