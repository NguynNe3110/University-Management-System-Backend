package com.university.student.api;

import jakarta.validation.constraints.*;

public record StudentProfileRequest(
        @NotBlank @Size(max = 32) String studentCode,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 36) String programId,
        @Pattern(regexp = "ACTIVE|INACTIVE") @NotNull String status,
        @Size(max = 36) String administrativeClassId,
        @Min(0) long version) {}
