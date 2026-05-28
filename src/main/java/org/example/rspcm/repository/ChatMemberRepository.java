package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
}
