package com.university.academic.api;

import jakarta.validation.constraints.*;

public record SemesterRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 32) String academicYear,
        @Min(1) int term,
        @Min(0) long version) {}
