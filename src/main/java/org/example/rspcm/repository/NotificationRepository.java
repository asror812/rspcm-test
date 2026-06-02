package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByRecipientIdAndReadFalse(Long userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.recipient.id = :userId and n.read = false")
    void markAllReadByUserId(Long userId);
}
