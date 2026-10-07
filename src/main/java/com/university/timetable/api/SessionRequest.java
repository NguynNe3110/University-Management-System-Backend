package com.university.timetable.api;

import jakarta.validation.constraints.*;

import java.time.*;

public record SessionRequest(
        @NotBlank @Size(max = 36) String teachingClassId,
        @Min(1) int sessionNumber,
        @NotBlank @Size(max = 36) String roomId,
        @NotNull LocalDate sessionDate,
        @Min(1) int startPeriod,
        @Min(1) int endPeriod,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {}
