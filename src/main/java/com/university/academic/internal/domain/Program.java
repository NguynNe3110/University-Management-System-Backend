package com.university.academic.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "program")
public class Program {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Program() {}

    public Program(String id, String code, String name, String departmentId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDepartmentId() { return departmentId; }
    public Instant getCreatedAt() { return createdAt; }
}
