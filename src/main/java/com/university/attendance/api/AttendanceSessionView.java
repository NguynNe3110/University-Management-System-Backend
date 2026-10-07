package com.university.attendance.api;

import java.time.Instant;

public record AttendanceSessionView(
        String id,
        String timetableSessionId,
        String qrToken,
        Instant expiresAt,
        String status,
        int rosterSize) {}
