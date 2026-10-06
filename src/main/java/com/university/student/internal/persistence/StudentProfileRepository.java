package com.university.student.internal.persistence;

import com.university.student.internal.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {
    Optional<StudentProfile> findByStudentCode(String studentCode);
}
