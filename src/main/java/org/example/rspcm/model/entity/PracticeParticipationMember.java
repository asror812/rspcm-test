package org.example.rspcm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "practice_participation_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participation_user",
                        columnNames = {"practice_participation_id", "user_id"}
                )
        }
)
public class PracticeParticipationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_participation_id",  nullable = false)
    private PracticeParticipation practiceParticipation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeParticipationMemberStatus status;
}
