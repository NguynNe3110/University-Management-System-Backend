package com.university.academic.api;

public record SemesterView(
    String id,
    String code,
    String academicYear,
    int term
) {}
