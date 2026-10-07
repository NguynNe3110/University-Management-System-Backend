package com.university.attendance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "attendance_record")
public class AttendanceRecord {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "attendance_session_id", length = 36, nullable = false)
    private String attendanceSessionId;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "check_in_time", nullable = false)
    private Instant checkInTime;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void correctedStatus(String status) {
        this.status = status;
    }

    protected AttendanceRecord() {}

    public AttendanceRecord(
            String id,
            String attendanceSessionId,
            String studentId,
            Instant checkInTime,
            String status) {
        this.id = id;
        this.attendanceSessionId = attendanceSessionId;
        this.studentId = studentId;
        this.checkInTime = checkInTime;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getAttendanceSessionId() {
        return attendanceSessionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
