package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeParticipationMemberRepository extends JpaRepository<PracticeParticipationMember, Long> {

    boolean existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
            Long practiceParticipationId,
            Long userId,
            PracticeMemberRole role,
            PracticeParticipationMemberStatus status
    );

    boolean existsByPracticeParticipationIdAndUserIdAndStatus(
            Long practiceParticipationId,
            Long userId,
            PracticeParticipationMemberStatus status
    );

    boolean existsByPracticeParticipationIdAndStatusNot(Long practiceParticipationId, PracticeParticipationMemberStatus status);

    Optional<PracticeParticipationMember> findByPracticeParticipationIdAndUserId(Long practiceParticipationId, Long userId);

    boolean existsByPracticeParticipationExamIdAndUserIdAndStatusAndPracticeParticipationIdNot(
            Long examId,
            Long userId,
            PracticeParticipationMemberStatus status,
            Long practiceParticipationId
    );
}
