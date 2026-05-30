package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    boolean existsByPracticeParticipationExamIdAndUserIdAndStatus(
            Long examId,
            Long userId,
            PracticeParticipationMemberStatus status
    );

    long countByPracticeParticipationIdAndStatusNot(
            Long practiceParticipationId,
            PracticeParticipationMemberStatus status
    );

    long countByPracticeParticipationIdAndStatus(
            Long practiceParticipationId,
            PracticeParticipationMemberStatus status
    );

    Optional<PracticeParticipationMember> findByPracticeParticipationExamIdAndUserIdAndStatus(
            Long examId,
            Long userId,
            PracticeParticipationMemberStatus status
    );

    Optional<PracticeParticipationMember> findByPracticeParticipationExamIdAndUserIdAndStatusNot(
            Long examId,
            Long userId,
            PracticeParticipationMemberStatus status
    );

    @EntityGraph(attributePaths = {
            "practiceParticipation",
            "practiceParticipation.exam",
            "practiceParticipation.examPractice",
            "practiceParticipation.examPractice.practice",
            "user"
    })
    List<PracticeParticipationMember> findByUserIdAndStatusNot(Long userId, PracticeParticipationMemberStatus status);

    @EntityGraph(attributePaths = {
            "practiceParticipation",
            "practiceParticipation.exam",
            "practiceParticipation.examPractice",
            "practiceParticipation.examPractice.practice",
            "user"
    })
    List<PracticeParticipationMember> findByUserIdAndStatus(Long userId, PracticeParticipationMemberStatus status);

    List<PracticeParticipationMember> findByPracticeParticipationId(Long practiceParticipationId);

    Optional<PracticeParticipationMember> findByIdAndPracticeParticipationId(Long id, Long practiceParticipationId);

    void deleteByPracticeParticipationId(Long practiceParticipationId);

    /**
     * Returns all ACCEPTED members of participations where the practice has schedulingRequired=true
     * AND the member has no logbook entry for today yet.
     */
    @Query("""
            SELECT DISTINCT m FROM PracticeParticipationMember m
            JOIN FETCH m.user u
            JOIN m.practiceParticipation pp
            JOIN pp.examPractice ep
            JOIN ep.practice p
            WHERE m.status = org.example.rspcm.model.enums.PracticeParticipationMemberStatus.ACCEPTED
              AND p.schedulingRequired = true
              AND NOT EXISTS (
                  SELECT e FROM PracticeLogbookEntry e
                  JOIN e.logbook lb
                  WHERE lb.student.id = u.id
                    AND lb.practice.id = p.id
                    AND e.entryDate = :today
              )
            """)
    List<PracticeParticipationMember> findMembersNeedingLogbookReminderToday(LocalDate today);

    /**
     * Returns ACCEPTED members of participations whose exam ends between fromAt and toAt
     * and who have no submission yet.
     */
    @Query("""
            SELECT DISTINCT m FROM PracticeParticipationMember m
            JOIN FETCH m.user u
            JOIN m.practiceParticipation pp
            JOIN pp.exam ex
            WHERE m.status = org.example.rspcm.model.enums.PracticeParticipationMemberStatus.ACCEPTED
              AND ex.endAt IS NOT NULL
              AND ex.endAt >= :fromAt
              AND ex.endAt < :toAt
              AND NOT EXISTS (
                  SELECT s FROM PracticeSubmission s
                  WHERE s.examParticipation.id = pp.id
                    AND s.status = org.example.rspcm.model.enums.PracticeSubmissionStatus.GRADED
              )
            """)
    List<PracticeParticipationMember> findMembersWithUpcomingDeadline(LocalDateTime fromAt, LocalDateTime toAt);
}
