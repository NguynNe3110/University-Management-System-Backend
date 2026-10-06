package com.university.academic.internal.persistence;

import com.university.academic.internal.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, String> {
    Optional<Semester> findByCode(String code);
}
