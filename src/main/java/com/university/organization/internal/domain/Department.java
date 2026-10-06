package com.university.organization.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Department() {}

    public Department(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
