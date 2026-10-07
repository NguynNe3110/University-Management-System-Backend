package com.university.tuition.internal.persistence;

import com.university.tuition.internal.domain.TuitionFee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select f from TuitionFee f where f.id=:id")
    Optional<TuitionFee> lockById(@org.springframework.data.repository.query.Param("id") String id);

    Optional<TuitionFee> findByStudentIdAndSemesterId(String studentId, String semesterId);

    List<TuitionFee> findByStudentId(String studentId);
}
