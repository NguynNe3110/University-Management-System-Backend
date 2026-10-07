package com.university.academic.api;

import jakarta.validation.constraints.*;

public record ProgramRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 36) String departmentId,
        @Min(0) long version) {}
