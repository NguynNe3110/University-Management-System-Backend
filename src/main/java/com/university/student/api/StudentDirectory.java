package com.university.student.api;

import java.util.List;
import java.util.Optional;

public interface StudentDirectory {
    Optional<StudentProfileView> findByStudentCode(String studentCode);
    Optional<StudentProfileView> findById(String id);
    List<StudentProfileView> findAllStudents();
}
