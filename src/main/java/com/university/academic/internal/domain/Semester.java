package com.university.academic.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "semester")
public class Semester {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "academic_year", length = 32, nullable = false)
    private String academicYear;

    @Column(name = "term", nullable = false)
    private int term;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @jakarta.persistence.Version private long version;

    public long getVersion() {
        return version;
    }

    public void revise(String code, String academicYear, int term, long expectedVersion) {
        if (version != expectedVersion)
            throw com.university.shared.exception.BusinessException.conflict("Stale version");
        this.code = code.trim();
        this.academicYear = academicYear.trim();
        this.term = term;
    }

    protected Semester() {}

    public Semester(String id, String code, String academicYear, int term) {
        this.id = id;
        this.code = code;
        this.academicYear = academicYear;
        this.term = term;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public int getTerm() {
        return term;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
