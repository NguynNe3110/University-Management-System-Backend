package com.university.academic.api;

import java.util.List;

public record CurriculumView(String programId, long version, List<Course> courses) {
    public record Course(String courseId, List<String> prerequisiteCourseIds) {}
}
