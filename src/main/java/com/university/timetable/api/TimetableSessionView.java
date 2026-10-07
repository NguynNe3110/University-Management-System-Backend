package com.university.timetable.api;

import java.time.LocalDate;

public record TimetableSessionView(
        String id,
        String teachingClassId,
        int sessionNumber,
        String roomId,
        LocalDate sessionDate,
        int startPeriod,
        int endPeriod,
        String status,
        java.time.Instant startsAt,
        java.time.Instant endsAt,
        long version) {}
