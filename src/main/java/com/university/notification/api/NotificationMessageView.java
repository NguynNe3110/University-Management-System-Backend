package com.university.notification.api;

import java.time.Instant;

public record NotificationMessageView(
    String id,
    String recipientId,
    String title,
    String content,
    String referenceModule,
    String referenceId,
    boolean isRead,
    Instant createdAt
) {}
