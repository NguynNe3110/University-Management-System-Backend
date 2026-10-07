package com.university.enrollment.api;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record WindowRequest(
        @NotBlank @Size(max = 36) String semesterId,
        @NotBlank @Size(max = 36) String programId,
        @NotNull Instant opensAt,
        @NotNull Instant closesAt,
        @NotNull Instant cancellationDeadline,
        @Min(1) int maxCredits,
        @NotNull List<@Pattern(regexp = "[0-9a-fA-F-]{36}") String> prerequisiteCourseIds,
        @DecimalMin("0") @DecimalMax("10") double minimumPassingScore) {}
