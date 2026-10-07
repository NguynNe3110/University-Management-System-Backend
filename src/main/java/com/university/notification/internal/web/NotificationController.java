package com.university.notification.internal.web;

import com.university.notification.api.NotificationMessageView;
import com.university.notification.api.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/by-recipient/{recipientId}")
    public ResponseEntity<List<NotificationMessageView>> getNotificationsByRecipient(
            @PathVariable String recipientId) {
        com.university.shared.security.Access.requireSelf("USER", recipientId);
        return ResponseEntity.ok(notificationService.findByRecipientId(recipientId));
    }
}
