package com.university.teachingclass.internal.persistence;

import com.university.teachingclass.internal.domain.TeachingClass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from TeachingClass c where c.id=:id")
    Optional<TeachingClass> lockById(
            @org.springframework.data.repository.query.Param("id") String id);

    Optional<TeachingClass> findByCode(String code);

    List<TeachingClass> findBySemesterId(String semesterId);
}
