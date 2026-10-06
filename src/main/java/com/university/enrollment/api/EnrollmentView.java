package com.university.enrollment.api;

import java.time.Instant;

public record EnrollmentView(
    String id,
    String studentId,
    String teachingClassId,
    String status,
    Instant enrolledAt
) {}
