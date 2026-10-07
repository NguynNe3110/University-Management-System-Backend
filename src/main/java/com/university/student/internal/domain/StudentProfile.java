package com.university.student.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "student_profile")
public class StudentProfile {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "student_code", length = 32, nullable = false, unique = true)
    private String studentCode;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "program_id", length = 36, nullable = false)
    private String programId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @jakarta.persistence.Version private long version;

    public long getVersion() {
        return version;
    }

    public void revise(
            String studentCode,
            String fullName,
            String email,
            String programId,
            String status,
            long expectedVersion) {
        if (version != expectedVersion)
            throw com.university.shared.exception.BusinessException.conflict("Stale version");
        this.studentCode = studentCode.trim();
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.programId = programId.trim();
        this.status = status.trim();
    }

    @Column(name = "administrative_class_id", length = 36)
    private String administrativeClassId;

    public String getAdministrativeClassId() {
        return administrativeClassId;
    }

    public void administrativeClass(String id) {
        administrativeClassId = id;
    }

    protected StudentProfile() {}

    public StudentProfile(
            String id,
            String studentCode,
            String fullName,
            String email,
            String programId,
            String status) {
        this.id = id;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.email = email;
        this.programId = programId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getProgramId() {
        return programId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
