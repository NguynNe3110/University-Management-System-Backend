package com.university.grade.api;

import java.util.List;
import java.util.Optional;

public interface GradeBook {
    Optional<StudentGradeView> findGrade(String studentId, String teachingClassId);
    List<StudentGradeView> findGradesByStudent(String studentId);
}
