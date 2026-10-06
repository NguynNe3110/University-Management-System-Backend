package com.university.enrollment.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_enrollment")
public class StudentEnrollment {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "teaching_class_id", length = 36, nullable = false)
    private String teachingClassId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt = Instant.now();

    protected StudentEnrollment() {}

    public StudentEnrollment(String id, String studentId, String teachingClassId, String status) {
        this.id = id;
        this.studentId = studentId;
        this.teachingClassId = teachingClassId;
        this.status = status;
        this.enrolledAt = Instant.now();
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getTeachingClassId() { return teachingClassId; }
    public String getStatus() { return status; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
