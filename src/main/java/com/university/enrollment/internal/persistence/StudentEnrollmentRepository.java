package com.university.enrollment.internal.persistence;

import com.university.enrollment.internal.domain.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, String> {
    List<StudentEnrollment> findByStudentId(String studentId);
    List<StudentEnrollment> findByTeachingClassId(String teachingClassId);
    Optional<StudentEnrollment> findByStudentIdAndTeachingClassId(String studentId, String teachingClassId);
    long countByTeachingClassIdAndStatus(String teachingClassId, String status);
}
