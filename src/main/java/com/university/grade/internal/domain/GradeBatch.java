package com.university.grade.internal.domain;

import com.university.grade.api.GradeBatchRequest;

import jakarta.persistence.*;

@Entity
@Table(name = "grade_batch")
public class GradeBatch {
    @Id
    @Column(name = "teaching_class_id", length = 36)
    private String classId;

    @Column(name = "attendance_weight", nullable = false)
    private double attendanceWeight;

    @Column(name = "midterm_weight", nullable = false)
    private double midtermWeight;

    @Column(name = "final_weight", nullable = false)
    private double finalWeight;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(name = "submitted_by", length = 64)
    private String submittedBy;

    @Column(name = "published_by", length = 64)
    private String publishedBy;

    @Version private long version;

    protected GradeBatch() {}

    public GradeBatch(String id) {
        classId = id;
        status = "DRAFT";
    }

    public void configure(GradeBatchRequest r) {
        attendanceWeight = r.attendanceWeight();
        midtermWeight = r.midtermWeight();
        finalWeight = r.finalWeight();
    }

    public void submit(String by) {
        status = "SUBMITTED";
        submittedBy = by;
    }

    public void publish(String by) {
        status = "PUBLISHED";
        publishedBy = by;
    }

    public String getClassId() {
        return classId;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public long getVersion() {
        return version;
    }

    public double getAttendanceWeight() {
        return attendanceWeight;
    }

    public double getMidtermWeight() {
        return midtermWeight;
    }

    public double getFinalWeight() {
        return finalWeight;
    }
}
