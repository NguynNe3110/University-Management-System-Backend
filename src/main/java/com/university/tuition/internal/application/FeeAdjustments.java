package com.university.tuition.internal.application;

import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.tuition.api.*;
import com.university.tuition.internal.persistence.TuitionFeeRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class FeeAdjustments {
    private final TuitionFeeRepository fees;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;
    private final ApplicationEventPublisher events;

    public FeeAdjustments(
            TuitionFeeRepository fees,
            JdbcTemplate jdbc,
            TechnicalAudit audit,
            ApplicationEventPublisher events) {
        this.fees = fees;
        this.jdbc = jdbc;
        this.audit = audit;
        this.events = events;
    }

    public FeeAdjustmentView propose(String feeId, BigDecimal delta, String reason) {
        var f = fees.findById(feeId).orElseThrow(() -> BusinessException.missing("Tuition fee"));
        require("FINANCE_STAFF", f.getStudentId(), f.getSemesterId());
        if (delta.signum() == 0) throw new IllegalArgumentException("Adjustment cannot be zero");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO fee_adjustment(id,tuition_fee_id,delta,reason,proposed_by,status)"
                        + " VALUES (?,?,?,?,?,'PENDING')",
                id,
                feeId,
                delta,
                reason,
                Access.username());
        audit.record("tuition", id, "PROPOSE_ADJUSTMENT", reason);
        return row(id);
    }

    public FeeAdjustmentView decide(String id, boolean approve, String reason) {
        var p = row(id);
        var f = fees.lockById(p.tuitionFeeId()).orElseThrow();
        jdbc.queryForObject(
                "SELECT id FROM fee_adjustment WHERE id=? FOR UPDATE", String.class, id);
        p = row(id);
        require("FINANCE_APPROVER", f.getStudentId(), f.getSemesterId());
        if (Access.username().equals(p.proposedBy()))
            throw BusinessException.conflict("Proposer cannot approve own fee adjustment");
        if (!p.status().equals("PENDING"))
            throw BusinessException.conflict("Adjustment already decided");
        if (approve) {
            f.adjust(p.delta());
            fees.flush();
            events.publishEvent(
                    new TuitionIssued(
                            f.getStudentId(),
                            f.getId(),
                            "Nghĩa vụ học phí đã được điều chỉnh theo quyết định."));
        }
        jdbc.update(
                "UPDATE fee_adjustment SET"
                    + " status=?,decided_by=?,decision_reason=?,decided_at=CURRENT_TIMESTAMP WHERE"
                    + " id=?",
                approve ? "APPROVED" : "REJECTED",
                Access.username(),
                reason,
                id);
        audit.record("tuition", id, "DECIDE_ADJUSTMENT", reason);
        return row(id);
    }

    @Transactional(readOnly = true)
    public FeeAdjustmentView read(String id) {
        var p = row(id);
        var f = fees.findById(p.tuitionFeeId()).orElseThrow();
        if (!Access.self("STUDENT", f.getStudentId())) {
            try {
                require("FINANCE_STAFF", f.getStudentId(), f.getSemesterId());
            } catch (org.springframework.security.access.AccessDeniedException e) {
                require("FINANCE_APPROVER", f.getStudentId(), f.getSemesterId());
            }
        }
        return p;
    }

    private void require(String role, String student, String semester) {
        if (!Access.can(role, "STUDENT", student)) Access.require(role, "SEMESTER", semester);
    }

    private FeeAdjustmentView row(String id) {
        var list =
                jdbc.query(
                        "SELECT * FROM fee_adjustment WHERE id=?",
                        (rs, n) ->
                                new FeeAdjustmentView(
                                        rs.getString("id"),
                                        rs.getString("tuition_fee_id"),
                                        rs.getBigDecimal("delta"),
                                        rs.getString("reason"),
                                        rs.getString("proposed_by"),
                                        rs.getString("status")),
                        id);
        if (list.isEmpty()) throw BusinessException.missing("Fee adjustment");
        return list.getFirst();
    }
}
