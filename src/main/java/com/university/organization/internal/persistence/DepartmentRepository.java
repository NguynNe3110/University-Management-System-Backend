package com.university.organization.internal.persistence;

import com.university.organization.internal.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, String> {
    Optional<Department> findByCode(String code);
}
