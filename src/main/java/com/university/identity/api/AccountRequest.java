package com.university.identity.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record AccountRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 36) String studentId,
        @Size(max = 36) String lecturerId,
        @NotNull @Size(min = 1, max = 20) List<@Valid Grant> grants) {
    @Override
    public String toString() {
        return "AccountRequest[username="
                + username
                + ", password=<redacted>, grants="
                + grants
                + "]";
    }

    public record Grant(
            @NotBlank String role,
            @NotBlank String scopeType,
            @NotBlank @Size(max = 36) String scopeId,
            @NotNull Instant validFrom,
            @NotNull Instant validUntil) {}
}
