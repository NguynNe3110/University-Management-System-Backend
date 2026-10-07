package com.university.attendance.internal.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "attendance_session")
public class AttendanceSession {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "qr_token", length = 255, nullable = false)
    private String qrToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(length = 32, nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(
            name = "attendance_roster",
            joinColumns = @JoinColumn(name = "attendance_session_id"))
    @Column(name = "student_id", length = 36)
    private Set<String> studentIds = new HashSet<>();

    protected AttendanceSession() {}

    public AttendanceSession(
            String sessionId, String token, Instant expires, Collection<String> roster) {
        this.id = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        qrToken = token;
        expiresAt = expires;
        status = "OPEN";
        studentIds.addAll(roster);
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public Set<String> getStudentIds() {
        return Set.copyOf(studentIds);
    }

    public void close() {
        status = "CLOSED";
    }
}
