package com.university.grade.internal.persistence;

import com.university.grade.internal.domain.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, String> {
    Optional<StudentGrade> findByStudentIdAndTeachingClassId(String studentId, String teachingClassId);
    List<StudentGrade> findByStudentId(String studentId);
}
