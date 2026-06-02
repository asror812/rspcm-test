package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.notification.NotificationResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Notification;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.NotificationType;
import org.example.rspcm.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final MessageService messageService;

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(User user, Pageable pageable) {
        return repository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return repository.countByRecipientIdAndReadFalse(user.getId());
    }

    // ── Mark read ─────────────────────────────────────────────────────────────

    @Transactional
    public NotificationResponse markOneRead(Long notificationId, User user) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено: " + notificationId));
        if (!n.getRecipient().getId().equals(user.getId())) {
            throw new ErrorMessageException(messageService.get("error.no.access"), ErrorCodes.Forbidden);
        }
        n.setRead(true);
        return toResponse(repository.save(n));
    }

    @Transactional
    public void markAllRead(User user) {
        repository.markAllReadByUserId(user.getId());
    }

    // ── Create (called by other services) ────────────────────────────────────

    @Transactional
    public void create(User recipient, String title, String body,
                       NotificationType type, Long referenceId) {
        Notification n = Notification.builder()
                .recipient(recipient)
                .title(title)
                .body(body)
                .type(type)
                .referenceId(referenceId)
                .build();
        repository.save(n);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getType(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
