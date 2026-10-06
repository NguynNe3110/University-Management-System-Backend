package com.university.teachingclass.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "teaching_class")
public class TeachingClass {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 64, nullable = false, unique = true)
    private String code;

    @Column(name = "course_id", length = 36, nullable = false)
    private String courseId;

    @Column(name = "semester_id", length = 36, nullable = false)
    private String semesterId;

    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "lecturer_id", length = 36)
    private String lecturerId;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TeachingClass() {}

    public TeachingClass(String id, String code, String courseId, String semesterId, String roomId, String lecturerId, int maxCapacity, String status) {
        this.id = id;
        this.code = code;
        this.courseId = courseId;
        this.semesterId = semesterId;
        this.roomId = roomId;
        this.lecturerId = lecturerId;
        this.maxCapacity = maxCapacity;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getCourseId() { return courseId; }
    public String getSemesterId() { return semesterId; }
    public String getRoomId() { return roomId; }
    public String getLecturerId() { return lecturerId; }
    public int getMaxCapacity() { return maxCapacity; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
