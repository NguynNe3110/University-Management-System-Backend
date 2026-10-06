package com.university.notification.api;

import java.util.List;

public interface NotificationService {
    List<NotificationMessageView> findByRecipientId(String recipientId);
}
