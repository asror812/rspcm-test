package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ChatMember;
import org.example.rspcm.model.enums.ChatMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
    void deleteByChatId(Long chatId);
    void deleteByChatIdAndRole(Long chatId, ChatMemberRole role);
    void deleteByChatIdAndRoleAndUserIdNotIn(Long chatId, ChatMemberRole role, Set<Long> userIds);

    @Query("""
            select cm.chat.id, count(cm.id)
            from ChatMember cm
            where cm.chat.id in :chatIds
            group by cm.chat.id
            """)
    List<Object[]> countMembersByChatIds(Collection<Long> chatIds);
}
