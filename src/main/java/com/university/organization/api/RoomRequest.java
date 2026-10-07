package com.university.organization.api;

import jakarta.validation.constraints.*;

public record RoomRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 64) String building,
        @Min(1) int capacity,
        @Min(0) long version) {}
