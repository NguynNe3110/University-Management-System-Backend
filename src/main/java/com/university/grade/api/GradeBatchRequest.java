package com.university.grade.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record GradeBatchRequest(
        @DecimalMin("0") @DecimalMax("1") double attendanceWeight,
        @DecimalMin("0") @DecimalMax("1") double midtermWeight,
        @DecimalMin("0") @DecimalMax("1") double finalWeight,
        @NotNull @Size(min = 1, max = 1000) List<@Valid Entry> grades,
        @Min(0) long version) {
    public record Entry(
            @NotBlank @Size(max = 36) String studentId,
            @DecimalMin("0") @DecimalMax("10") double attendanceScore,
            @DecimalMin("0") @DecimalMax("10") double midtermScore,
            @DecimalMin("0") @DecimalMax("10") double finalScore) {}
}
