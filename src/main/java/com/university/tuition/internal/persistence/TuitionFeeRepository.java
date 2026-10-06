package com.university.tuition.internal.persistence;

import com.university.tuition.internal.domain.TuitionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, String> {
    Optional<TuitionFee> findByStudentIdAndSemesterId(String studentId, String semesterId);
    List<TuitionFee> findByStudentId(String studentId);
}
