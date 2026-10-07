package com.university.teaching.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "teaching_log")
public class TeachingLog {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "timetable_session_id", length = 36, nullable = false, unique = true)
    private String timetableSessionId;

    @Column(name = "lecturer_id", length = 36, nullable = false)
    private String lecturerId;

    @Column(name = "actual_hours", nullable = false)
    private int actualHours;

    @Column(name = "content_summary", columnDefinition = "TEXT", nullable = false)
    private String contentSummary;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "submitted_by", length = 64)
    private String submittedBy;

    @Column(name = "decided_by", length = 64)
    private String decidedBy;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void submitted(String actor) {
        submittedBy = actor;
    }

    public void decide(String status, String actor, String reason) {
        this.status = status;
        decidedBy = actor;
        decisionReason = reason;
    }

    protected TeachingLog() {}

    public TeachingLog(
            String id,
            String timetableSessionId,
            String lecturerId,
            int actualHours,
            String contentSummary,
            String status) {
        this.id = id;
        this.timetableSessionId = timetableSessionId;
        this.lecturerId = lecturerId;
        this.actualHours = actualHours;
        this.contentSummary = contentSummary;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTimetableSessionId() {
        return timetableSessionId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public int getActualHours() {
        return actualHours;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
