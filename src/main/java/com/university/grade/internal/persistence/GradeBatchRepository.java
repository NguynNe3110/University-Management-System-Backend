package com.university.grade.internal.persistence;

import com.university.grade.internal.domain.GradeBatch;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GradeBatchRepository extends JpaRepository<GradeBatch, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from GradeBatch b where b.classId=:id")
    Optional<GradeBatch> lockById(@Param("id") String id);
}
