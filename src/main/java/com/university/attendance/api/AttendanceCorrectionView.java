package com.university.attendance.api;

public record AttendanceCorrectionView(
        String id,
        String attendanceSessionId,
        String studentId,
        String requestedStatus,
        String reason,
        String proposedBy,
        String status) {}
