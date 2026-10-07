package com.university.enrollment.api;

public interface GradeEligibility {
    boolean hasPassedCourse(String studentId, String courseId, double minimumScore);
}
