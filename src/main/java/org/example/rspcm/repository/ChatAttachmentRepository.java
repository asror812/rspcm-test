package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {
    Optional<ChatAttachment> findByStoredName(String storedName);
}
