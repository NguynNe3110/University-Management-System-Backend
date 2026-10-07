package com.university.grade.api;

public record GradeCorrectionView(
        String id,
        String teachingClassId,
        String studentId,
        double attendanceScore,
        double midtermScore,
        double finalScore,
        String reason,
        String proposedBy,
        long expectedRevision,
        String status) {}
