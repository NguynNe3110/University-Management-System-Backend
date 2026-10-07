package com.university.organization.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "building", length = 64, nullable = false)
    private String building;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @jakarta.persistence.Version private long version;

    public long getVersion() {
        return version;
    }

    public void revise(String code, String building, int capacity, long expectedVersion) {
        if (version != expectedVersion)
            throw com.university.shared.exception.BusinessException.conflict("Stale version");
        this.code = code.trim();
        this.building = building.trim();
        this.capacity = capacity;
    }

    protected Room() {}

    public Room(String id, String code, String building, int capacity) {
        this.id = id;
        this.code = code;
        this.building = building;
        this.capacity = capacity;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getBuilding() {
        return building;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
