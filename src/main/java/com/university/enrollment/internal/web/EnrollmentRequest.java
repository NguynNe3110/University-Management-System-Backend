package com.university.enrollment.internal.web;

import jakarta.validation.constraints.*;

public record EnrollmentRequest(
        @NotBlank @Size(max = 36) String studentId,
        @NotBlank @Size(max = 36) String teachingClassId,
        @NotBlank @Size(max = 36) String windowId) {}
