package com.university.notification.internal.persistence;

import com.university.notification.internal.domain.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, String> {
    List<NotificationMessage> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
}
