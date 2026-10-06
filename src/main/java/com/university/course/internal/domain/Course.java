package com.university.course.internal.domain;

import jakarta.persistence.*;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "courses", schema = "course")
public class Course {
    @Id
    private UUID id;
    @Column(nullable = false, length = 30, updatable = false)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false)
    private int credits;
    @Column(nullable = false)
    private boolean active;
    @Version
    private Long version;

    protected Course() {}

    public Course(String code, String name, int credits) {
        this.id = UUID.randomUUID();
        this.code = normalizeCode(code);
        setDetails(name, credits);
        this.active = true;
    }

    public static String normalizeCode(String code) {
        if (code == null) throw new IllegalArgumentException("Course code is required");
        var normalized = code.strip().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,29}")) {
            throw new IllegalArgumentException("Course code must have 1-30 letters, digits, underscores or hyphens");
        }
        return normalized;
    }

    public void update(String name, int credits) {
        if (!active) throw new IllegalStateException("Archived course cannot be edited");
        setDetails(name, credits);
    }

    private void setDetails(String name, int credits) {
        if (name == null || name.isBlank() || name.strip().length() > 200) {
            throw new IllegalArgumentException("Course name must have 1-200 characters");
        }
        if (credits < 1 || credits > 30) {
            throw new IllegalArgumentException("Credits must be between 1 and 30");
        }
        this.name = name.strip();
        this.credits = credits;
    }

    public void archive() { this.active = false; }
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getCredits() { return credits; }
    public boolean isActive() { return active; }
    public long getVersion() { return version == null ? 0 : version; }
}
