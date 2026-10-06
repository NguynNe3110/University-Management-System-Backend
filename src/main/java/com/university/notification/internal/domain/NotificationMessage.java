package com.university.notification.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_message")
public class NotificationMessage {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "recipient_id", length = 36, nullable = false)
    private String recipientId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "reference_module", length = 64)
    private String referenceModule;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected NotificationMessage() {}

    public NotificationMessage(String id, String recipientId, String title, String content, String referenceModule, String referenceId) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.content = content;
        this.referenceModule = referenceModule;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRecipientId() { return recipientId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getReferenceModule() { return referenceModule; }
    public String getReferenceId() { return referenceId; }
    public boolean isRead() { return isRead; }
    public Instant getCreatedAt() { return createdAt; }
}
