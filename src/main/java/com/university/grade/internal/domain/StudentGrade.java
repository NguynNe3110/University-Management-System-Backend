package com.university.grade.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_grade")
public class StudentGrade {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "teaching_class_id", length = 36, nullable = false)
    private String teachingClassId;

    @Column(name = "attendance_score")
    private Double attendanceScore;

    @Column(name = "midterm_score")
    private Double midtermScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected StudentGrade() {}

    public StudentGrade(String id, String studentId, String teachingClassId, Double attendanceScore, Double midtermScore, Double finalScore, Double totalScore, String status) {
        this.id = id;
        this.studentId = studentId;
        this.teachingClassId = teachingClassId;
        this.attendanceScore = attendanceScore;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.totalScore = totalScore;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getTeachingClassId() { return teachingClassId; }
    public Double getAttendanceScore() { return attendanceScore; }
    public Double getMidtermScore() { return midtermScore; }
    public Double getFinalScore() { return finalScore; }
    public Double getTotalScore() { return totalScore; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
