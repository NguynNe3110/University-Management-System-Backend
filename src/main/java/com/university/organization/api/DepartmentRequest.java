package com.university.organization.api;

import jakarta.validation.constraints.*;

public record DepartmentRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 255) String name,
        @Min(0) long version) {}
