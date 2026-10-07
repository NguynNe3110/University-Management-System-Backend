package com.university.lecturer.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "lecturer_profile")
public class LecturerProfile {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "lecturer_code", length = 32, nullable = false, unique = true)
    private String lecturerCode;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @jakarta.persistence.Version private long version;

    public long getVersion() {
        return version;
    }

    public void revise(
            String lecturerCode,
            String fullName,
            String email,
            String departmentId,
            String status,
            long expectedVersion) {
        if (version != expectedVersion)
            throw com.university.shared.exception.BusinessException.conflict("Stale version");
        this.lecturerCode = lecturerCode.trim();
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.departmentId = departmentId.trim();
        this.status = status.trim();
    }

    protected LecturerProfile() {}

    public LecturerProfile(
            String id,
            String lecturerCode,
            String fullName,
            String email,
            String departmentId,
            String status) {
        this.id = id;
        this.lecturerCode = lecturerCode;
        this.fullName = fullName;
        this.email = email;
        this.departmentId = departmentId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getLecturerCode() {
        return lecturerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
