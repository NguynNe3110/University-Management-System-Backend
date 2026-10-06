package com.university.lecturer.api;

public record LecturerProfileView(
    String id,
    String lecturerCode,
    String fullName,
    String email,
    String departmentId,
    String status
) {}
