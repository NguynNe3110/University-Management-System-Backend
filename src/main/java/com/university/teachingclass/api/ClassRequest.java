package com.university.teachingclass.api;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ClassRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 36) String courseId,
        @NotBlank @Size(max = 36) String semesterId,
        @NotBlank @Size(max = 36) String departmentId,
        @NotBlank @Size(max = 36) String lecturerId,
        @Min(1) int maxCapacity,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal tuitionRate) {}
