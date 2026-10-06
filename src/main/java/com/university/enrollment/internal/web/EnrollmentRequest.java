package com.university.enrollment.internal.web;

public record EnrollmentRequest(
    String studentId,
    String teachingClassId
) {}
