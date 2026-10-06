package com.university.enrollment.api;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRegistry {
    Optional<EnrollmentView> findById(String id);
    List<EnrollmentView> findByStudentId(String studentId);
    List<EnrollmentView> findByTeachingClassId(String teachingClassId);
    EnrollmentView enroll(String studentId, String teachingClassId);
    EnrollmentView cancel(String enrollmentId);
}
