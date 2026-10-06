package com.university.academic.api;

public record ProgramView(
    String id,
    String code,
    String name,
    String departmentId
) {}
