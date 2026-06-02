package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipation;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeParticipationRepository extends JpaRepository<PracticeParticipation, Long> {
    @EntityGraph(attributePaths = {"exam", "examPractice", "examPractice.practice"})
    Page<PracticeParticipation> findByExamId(Long examId, Pageable pageable);

    @EntityGraph(attributePaths = {"exam", "examPractice", "examPractice.practice"})
    Page<PracticeParticipation> findByExamIdAndStatus(Long examId, PracticeParticipationStatus status, Pageable pageable);

    Optional<PracticeParticipation> findByExamIdAndExamPracticeIsNotNull(Long examId);

    Optional<PracticeParticipation> findByExamIdAndMembersUserId(Long examId, Long userId);
}
