package com.university.tuition.internal.application;

import com.university.tuition.api.TuitionFeeView;
import com.university.tuition.api.TuitionLedger;
import com.university.tuition.internal.domain.TuitionFee;
import com.university.tuition.internal.persistence.TuitionFeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TuitionLedgerImpl implements TuitionLedger {

    private final TuitionFeeRepository tuitionFeeRepository;

    public TuitionLedgerImpl(TuitionFeeRepository tuitionFeeRepository) {
        this.tuitionFeeRepository = tuitionFeeRepository;
    }

    @Override
    public Optional<TuitionFeeView> findByStudentAndSemester(String studentId, String semesterId) {
        return tuitionFeeRepository.findByStudentIdAndSemesterId(studentId, semesterId).map(this::mapToView);
    }

    @Override
    public List<TuitionFeeView> findByStudentId(String studentId) {
        return tuitionFeeRepository.findByStudentId(studentId).stream().map(this::mapToView).toList();
    }

    private TuitionFeeView mapToView(TuitionFee t) {
        return new TuitionFeeView(t.getId(), t.getStudentId(), t.getSemesterId(), t.getAmountDue(), t.getAmountPaid(), t.getStatus());
    }
}
