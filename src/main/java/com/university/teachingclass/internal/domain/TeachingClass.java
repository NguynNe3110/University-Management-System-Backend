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

    @Column(name = "department_id", length = 36)
    private String departmentId;

    @Column(name = "tuition_rate", precision = 12, scale = 2)
    private java.math.BigDecimal tuitionRate;

    @jakarta.persistence.Version private long version;

    public String getDepartmentId() {
        return departmentId;
    }

    public java.math.BigDecimal getTuitionRate() {
        return tuitionRate;
    }

    public long getVersion() {
        return version;
    }

    public void configure(String departmentId, java.math.BigDecimal rate) {
        this.departmentId = departmentId;
        this.tuitionRate = rate;
    }

    public void assign(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    protected TeachingClass() {}

    public TeachingClass(
            String id,
            String code,
            String courseId,
            String semesterId,
            String roomId,
            String lecturerId,
            int maxCapacity,
            String status) {
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

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
