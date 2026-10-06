package com.university.notification.internal.application;

import com.university.notification.api.NotificationMessageView;
import com.university.notification.api.NotificationService;
import com.university.notification.internal.persistence.NotificationMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMessageRepository notificationMessageRepository;

    public NotificationServiceImpl(NotificationMessageRepository notificationMessageRepository) {
        this.notificationMessageRepository = notificationMessageRepository;
    }

    @Override
    public List<NotificationMessageView> findByRecipientId(String recipientId) {
        return notificationMessageRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
            .stream()
            .map(n -> new NotificationMessageView(
                n.getId(),
                n.getRecipientId(),
                n.getTitle(),
                n.getContent(),
                n.getReferenceModule(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
            ))
            .toList();
    }
}
