package com.university.lecturer.api;

import jakarta.validation.constraints.*;

public record LecturerProfileRequest(
        @NotBlank @Size(max = 32) String lecturerCode,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 36) String departmentId,
        @Pattern(regexp = "ACTIVE|INACTIVE") @NotNull String status,
        @Min(0) long version) {}
