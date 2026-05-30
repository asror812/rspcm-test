package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Chat;
import org.example.rspcm.model.enums.ChatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByStudyGroupIdAndTypeAndTitle(Long studyGroupId, ChatType type, String title);
    List<Chat> findAllByStudyGroupIdAndTypeOrderByIdAsc(Long studyGroupId, ChatType type);

    @Query("""
            select (count(cm) > 0)
            from ChatMember cm
            join cm.user u
            where cm.chat.id = :chatId
              and (u.universityId = :name or u.email = :name)
              and u.enabled = true
              and u.deleted = false
            """)
    boolean existsByIdAndMemberIdentifier(Long chatId, String name);

    @Query("""
            select c from Chat c
            join ChatMember cm on cm.chat.id = c.id
            join User u on u.id = cm.user.id
            where (u.email = :name or u.universityId = :name) and u.enabled = true and u.deleted = false
            order by c.id desc
            """)
    List<Chat> findAllByMemberIdentifier(String name);
}
