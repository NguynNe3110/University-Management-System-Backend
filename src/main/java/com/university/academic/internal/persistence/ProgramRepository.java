package com.university.academic.internal.persistence;

import com.university.academic.internal.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, String> {
    Optional<Program> findByCode(String code);
}
