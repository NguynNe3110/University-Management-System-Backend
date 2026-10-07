package com.university.grade.api;

public record StudentGradeView(
        String id,
        String studentId,
        String teachingClassId,
        Double attendanceScore,
        Double midtermScore,
        Double finalScore,
        Double totalScore,
        String status,
        long resultRevision) {}
