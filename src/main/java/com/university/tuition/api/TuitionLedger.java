package com.university.tuition.api;

import java.util.List;
import java.util.Optional;

public interface TuitionLedger {
    Optional<TuitionFeeView> findByStudentAndSemester(String studentId, String semesterId);
    List<TuitionFeeView> findByStudentId(String studentId);
}
