package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeParticipationMemberRepository extends JpaRepository<PracticeParticipationMember, Long> {

    Optional<PracticeParticipationMember> findByPracticeParticipationIdAndUserId(Long practiceParticipationId, Long userId);

    List<PracticeParticipationMember> findByPracticeParticipationId(Long practiceParticipationId);

    boolean existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
            Long practiceParticipationId,
            Long userId,
            PracticeMemberRole role,
            PracticeParticipationMemberStatus status
    );

    boolean existsByPracticeParticipationIdAndUserId(Long practiceParticipationId, Long userId);

    boolean existsByPracticeParticipationIdAndStatusNot(Long practiceParticipationId, PracticeParticipationMemberStatus status);
}
