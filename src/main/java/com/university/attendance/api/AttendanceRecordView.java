package com.university.attendance.api;

import java.time.Instant;

public record AttendanceRecordView(
    String id,
    String attendanceSessionId,
    String studentId,
    Instant checkInTime,
    String status
) {}
