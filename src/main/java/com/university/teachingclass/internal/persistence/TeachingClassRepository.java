package com.university.teachingclass.internal.persistence;

import com.university.teachingclass.internal.domain.TeachingClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, String> {
    Optional<TeachingClass> findByCode(String code);
    List<TeachingClass> findBySemesterId(String semesterId);
}
