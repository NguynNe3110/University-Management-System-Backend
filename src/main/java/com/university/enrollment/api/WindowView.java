package com.university.enrollment.api;

import java.time.Instant;
import java.util.List;

public record WindowView(
        String id,
        String semesterId,
        String programId,
        Instant opensAt,
        Instant closesAt,
        Instant cancellationDeadline,
        int maxCredits,
        List<String> prerequisiteCourseIds,
        double minimumPassingScore) {}
