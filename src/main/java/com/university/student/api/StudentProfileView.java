package com.university.student.api;

public record StudentProfileView(
    String id,
    String studentCode,
    String fullName,
    String email,
    String programId,
    String status
) {}
