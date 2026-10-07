package com.university.timetable.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "timetable_session")
public class TimetableSession {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "teaching_class_id", length = 36, nullable = false)
    private String teachingClassId;

    @Column(name = "session_number", nullable = false)
    private int sessionNumber;

    @Column(name = "room_id", length = 36, nullable = false)
    private String roomId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_period", nullable = false)
    private int startPeriod;

    @Column(name = "end_period", nullable = false)
    private int endPeriod;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @jakarta.persistence.Version private long version;

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getVersion() {
        return version;
    }

    public void timing(Instant start, Instant end, String actor) {
        startsAt = start;
        endsAt = end;
        createdBy = actor;
    }

    public void move(String room, Instant start, Instant end) {
        roomId = room;
        startsAt = start;
        endsAt = end;
        sessionDate = start.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();
    }

    public void setStatus(String value) {
        status = value;
    }

    protected TimetableSession() {}

    public TimetableSession(
            String id,
            String teachingClassId,
            int sessionNumber,
            String roomId,
            LocalDate sessionDate,
            int startPeriod,
            int endPeriod,
            String status) {
        this.id = id;
        this.teachingClassId = teachingClassId;
        this.sessionNumber = sessionNumber;
        this.roomId = roomId;
        this.sessionDate = sessionDate;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTeachingClassId() {
        return teachingClassId;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
