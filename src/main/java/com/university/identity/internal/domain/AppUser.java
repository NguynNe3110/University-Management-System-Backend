package com.university.identity.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "username", length = 64, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "roles", length = 255, nullable = false)
    private String roles;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "student_id", length = 36)
    private String studentId;

    @Column(name = "lecturer_id", length = 36)
    private String lecturerId;

    public String getStudentId() {
        return studentId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void link(String student, String lecturer) {
        this.studentId = student;
        this.lecturerId = lecturer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    protected AppUser() {}

    public AppUser(
            String id,
            String username,
            String passwordHash,
            String fullName,
            String email,
            String roles,
            String status) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRoles() {
        return roles;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
