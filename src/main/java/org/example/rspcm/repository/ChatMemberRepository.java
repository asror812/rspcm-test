package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ChatMember;
import org.example.rspcm.model.enums.ChatMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
    void deleteByChatIdAndRole(Long chatId, ChatMemberRole role);

    @Query("""
            select cm.chat.id, count(cm.id)
            from ChatMember cm
            where cm.chat.id in :chatIds
            group by cm.chat.id
            """)
    List<Object[]> countMembersByChatIds(Collection<Long> chatIds);
}
