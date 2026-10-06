package com.university.teaching.api;

public record TeachingLogView(
    String id,
    String timetableSessionId,
    String lecturerId,
    int actualHours,
    String contentSummary,
    String status
) {}
