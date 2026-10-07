package com.university.grade.api;

import java.util.List;

public record GradeBatchView(
        String teachingClassId,
        String status,
        long version,
        double attendanceWeight,
        double midtermWeight,
        double finalWeight,
        List<StudentGradeView> grades) {}
