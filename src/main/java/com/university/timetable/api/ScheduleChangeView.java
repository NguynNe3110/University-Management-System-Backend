package com.university.timetable.api;

import java.time.Instant;

public record ScheduleChangeView(
        String id,
        String timetableSessionId,
        boolean cancel,
        String roomId,
        Instant startsAt,
        Instant endsAt,
        String reason,
        String proposedBy,
        long expectedVersion,
        String status) {}
