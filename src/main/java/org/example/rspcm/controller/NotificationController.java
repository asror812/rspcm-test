package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.notification.NotificationResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /** Paginated list — newest first. */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(user, PageRequest.of(page, size)));
    }

    /** Number of unread notifications (used for the badge). */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user)));
    }

    /** Mark a single notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markOneRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.markOneRead(id, user));
    }

    /** Mark all of the current user's notifications as read. */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user);
        return ResponseEntity.noContent().build();
    }
}
