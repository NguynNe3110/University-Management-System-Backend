package com.university.enrollment.internal.domain;

import com.university.enrollment.api.*;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "registration_window")
public class RegistrationWindow {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "semester_id", length = 36, nullable = false)
    private String semesterId;

    @Column(name = "program_id", length = 36, nullable = false)
    private String programId;

    @Column(name = "opens_at", nullable = false)
    private Instant opensAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    @Column(name = "cancellation_deadline", nullable = false)
    private Instant cancellationDeadline;

    @Column(name = "max_credits", nullable = false)
    private int maxCredits;

    @Column(name = "prerequisites", columnDefinition = "TEXT", nullable = false)
    private String prerequisites;

    @Column(name = "minimum_passing_score", nullable = false)
    private double minimumPassingScore;

    protected RegistrationWindow() {}

    public RegistrationWindow(WindowRequest r) {
        id = UUID.randomUUID().toString();
        semesterId = r.semesterId();
        programId = r.programId();
        opensAt = r.opensAt();
        closesAt = r.closesAt();
        cancellationDeadline = r.cancellationDeadline();
        maxCredits = r.maxCredits();
        prerequisites = String.join(",", r.prerequisiteCourseIds());
        minimumPassingScore = r.minimumPassingScore();
    }

    public WindowView view() {
        return new WindowView(
                id,
                semesterId,
                programId,
                opensAt,
                closesAt,
                cancellationDeadline,
                maxCredits,
                prerequisites.isBlank() ? List.of() : List.of(prerequisites.split(",")),
                minimumPassingScore);
    }
}
