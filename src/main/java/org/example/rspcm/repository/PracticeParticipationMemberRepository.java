package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeParticipationMemberRepository extends JpaRepository<PracticeParticipationMember, Long> {
    boolean existsByPracticeParticipationIdAndUserId(Long practiceParticipationId, Long userId);

    boolean existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
            Long practiceParticipationId,
            Long userId,
            PracticeMemberRole role,
            PracticeParticipationMemberStatus status
    );

    boolean existsByPracticeParticipationIdAndStatusNot(Long practiceParticipationId, PracticeParticipationMemberStatus status);
}
