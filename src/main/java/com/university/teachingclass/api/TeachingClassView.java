package com.university.teachingclass.api;

public record TeachingClassView(
    String id,
    String code,
    String courseId,
    String semesterId,
    String roomId,
    String lecturerId,
    int maxCapacity,
    String status
) {}
