package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatIdOrderByIdAsc(Long chatId);
    Optional<ChatMessage> findTopByChatIdOrderByIdDesc(Long chatId);
}
