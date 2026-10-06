package com.university.reporting.api;

public record UniversityStatisticsView(
    long totalCourses,
    long totalStudents,
    long totalClasses
) {}
