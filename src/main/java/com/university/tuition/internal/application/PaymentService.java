package com.university.tuition.internal.application;

import com.university.academic.api.AcademicCatalog;
import com.university.enrollment.api.EnrollmentRegistry;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.student.api.StudentDirectory;
import com.university.teachingclass.api.TeachingClassDirectory;
import com.university.tuition.api.*;
import com.university.tuition.internal.domain.TuitionFee;
import com.university.tuition.internal.persistence.TuitionFeeRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class PaymentService {
    private final TuitionFeeRepository fees;
    private final TuitionLedger ledger;
    private final EnrollmentRegistry enrollment;
    private final TeachingClassDirectory classes;
    private final StudentDirectory students;
    private final AcademicCatalog academic;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;
    private final ApplicationEventPublisher events;
    private final boolean simulator;

    public PaymentService(
            TuitionFeeRepository fees,
            TuitionLedger ledger,
            EnrollmentRegistry enrollment,
            TeachingClassDirectory classes,
            StudentDirectory students,
            AcademicCatalog academic,
            JdbcTemplate jdbc,
            TechnicalAudit audit,
            ApplicationEventPublisher events,
            @Value("${payments.simulator-enabled:false}") boolean simulator) {
        this.fees = fees;
        this.ledger = ledger;
        this.enrollment = enrollment;
        this.classes = classes;
        this.students = students;
        this.academic = academic;
        this.jdbc = jdbc;
        this.audit = audit;
        this.events = events;
        this.simulator = simulator;
    }

    public void requireRead(String student, String semester) {
        if (!Access.self("STUDENT", student)
                && !Access.can("FINANCE_STAFF", "STUDENT", student)
                && !Access.can("FINANCE_STAFF", "SEMESTER", semester))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Fee outside assigned scope");
    }

    public TuitionFeeView issue(String student, String semester) {
        if (!Access.can("FINANCE_STAFF", "STUDENT", student))
            Access.require("FINANCE_STAFF", "SEMESTER", semester);
        if (students.findById(student).isEmpty()) throw BusinessException.missing("Student");
        if (academic.findSemesterById(semester).isEmpty())
            throw BusinessException.missing("Semester");
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "student:" + student);
        var f =
                fees.findByStudentIdAndSemesterId(student, semester)
                        .orElseGet(
                                () ->
                                        new TuitionFee(
                                                UUID.randomUUID().toString(),
                                                student,
                                                semester,
                                                BigDecimal.ZERO,
                                                BigDecimal.ZERO,
                                                "UNPAID"));
        fees.saveAndFlush(f);
        f = fees.lockById(f.getId()).orElseThrow();
        int added = 0;
        for (var e : enrollment.findByStudentId(student)) {
            if (!e.status().equals("ENROLLED")) continue;
            var c = classes.findClassById(e.teachingClassId()).orElseThrow();
            if (!c.semesterId().equals(semester)) continue;
            if (c.tuitionRate() == null)
                throw BusinessException.conflict("Class has no configured tuition amount");
            if (jdbc.queryForObject(
                            "SELECT count(*) FROM tuition_line WHERE tuition_fee_id=? AND"
                                    + " course_id=?",
                            Long.class,
                            f.getId(),
                            c.courseId())
                    == 0) {
                jdbc.update(
                        "INSERT INTO tuition_line(id,tuition_fee_id,course_id,enrollment_id,amount)"
                                + " VALUES (?,?,?,?,?)",
                        UUID.randomUUID().toString(),
                        f.getId(),
                        c.courseId(),
                        e.id(),
                        c.tuitionRate());
                f.addDue(c.tuitionRate());
                added++;
            }
        }
        fees.flush();
        audit.record("tuition", f.getId(), "ISSUE", "Added courses=" + added);
        if (added > 0)
            events.publishEvent(
                    new TuitionIssued(
                            student,
                            f.getId(),
                            "Học phí đã được phát hành; kiểm tra nghĩa vụ trên hệ thống."));
        return ledger.findByStudentAndSemester(student, semester).orElseThrow();
    }

    public PaymentView request(String feeId, String key, Instant expires) {
        if (!simulator)
            throw BusinessException.conflict(
                    "Payment simulator is disabled; no real gateway configured");
        var f = fees.lockById(feeId).orElseThrow(() -> BusinessException.missing("Tuition fee"));
        Access.requireSelf("STUDENT", f.getStudentId());
        if (!Access.authority("ROLE_STUDENT"))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Student role required");
        var existing =
                jdbc.query(
                        "SELECT id FROM payment_request WHERE tuition_fee_id=? AND request_key=?",
                        (rs, n) -> rs.getString(1),
                        feeId,
                        key);
        if (!existing.isEmpty()) return payment(existing.getFirst());
        var amount = f.getAmountDue().subtract(f.getAmountPaid());
        if (amount.signum() <= 0) throw BusinessException.conflict("No outstanding tuition");
        if (!expires.isAfter(Instant.now()))
            throw new IllegalArgumentException("Payment expiry must be in the future");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO"
                    + " payment_request(id,tuition_fee_id,request_key,amount,currency,status,expires_at)"
                    + " VALUES (?,?,?,?,'VND','PENDING',?)",
                id,
                feeId,
                key,
                amount,
                Timestamp.from(expires));
        audit.record("tuition", id, "PAYMENT_REQUEST", "SIMULATOR only");
        return payment(id);
    }

    public PaymentView read(String id) {
        var p = payment(id);
        var f = fees.findById(p.tuitionFeeId()).orElseThrow();
        requireRead(f.getStudentId(), f.getSemesterId());
        return p;
    }

    public PaymentView simulate(
            String id, String transactionId, BigDecimal amount, String currency) {
        if (!simulator) throw BusinessException.conflict("Payment simulator is disabled");
        payment(id); // Return the API's 404 before locking a missing request.
        jdbc.queryForObject(
                "SELECT id FROM payment_request WHERE id=? FOR UPDATE", String.class, id);
        var p = payment(id);
        var f = fees.lockById(p.tuitionFeeId()).orElseThrow();
        if (!Access.self("STUDENT", f.getStudentId())
                && !Access.can("FINANCE_STAFF", "STUDENT", f.getStudentId())
                && !Access.can("FINANCE_STAFF", "SEMESTER", f.getSemesterId()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Payment outside assigned scope");
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "payment-transaction:" + transactionId);
        var old =
                jdbc.query(
                        "SELECT payment_request_id,amount,currency FROM payment_transaction WHERE"
                                + " provider='SIMULATOR' AND transaction_id=?",
                        (rs, n) ->
                                new Object[] {
                                    rs.getString(1), rs.getBigDecimal(2), rs.getString(3)
                                },
                        transactionId);
        if (!old.isEmpty()) {
            var o = old.getFirst();
            if (!id.equals(o[0])
                    || amount.compareTo((BigDecimal) o[1]) != 0
                    || !currency.equals(o[2]))
                throw BusinessException.conflict("Transaction ID reused with a different payload");
            return payment(id);
        }
        // Capture the incoming transaction even if expired, mismatched or already paid.
        boolean match =
                p.status().equals("PENDING")
                        && Instant.now().isBefore(p.expiresAt())
                        && amount.compareTo(p.amount()) == 0
                        && currency.equals(p.currency())
                        && amount.compareTo(f.getAmountDue().subtract(f.getAmountPaid())) <= 0;
        String outcome = match ? "ALLOCATED" : "RECONCILIATION";
        jdbc.update(
                "INSERT INTO"
                    + " payment_transaction(id,provider,transaction_id,payment_request_id,amount,currency,outcome)"
                    + " VALUES (?,'SIMULATOR',?,?,?,?,?)",
                UUID.randomUUID().toString(),
                transactionId,
                id,
                amount,
                currency,
                outcome);
        if (match) {
            f.receive(amount);
            jdbc.update("UPDATE payment_request SET status='PAID' WHERE id=?", id);
            fees.flush();
        } else if (!p.status().equals("PAID"))
            jdbc.update("UPDATE payment_request SET status='RECONCILIATION' WHERE id=?", id);
        audit.record("tuition", id, "SIMULATED_RESULT", outcome + " transaction=" + transactionId);
        events.publishEvent(
                new TuitionIssued(
                        f.getStudentId(),
                        f.getId(),
                        match
                                ? "Thanh toán GIẢ LẬP đã được ghi nhận."
                                : "Giao dịch GIẢ LẬP đang chờ đối soát."));
        return payment(id);
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentTransactionView> reconciliation() {
        return jdbc
                .query(
                        "SELECT"
                            + " t.transaction_id,t.payment_request_id,f.student_id,f.semester_id,t.amount,t.currency,t.outcome,t.created_at"
                            + " FROM payment_transaction t JOIN payment_request p ON"
                            + " p.id=t.payment_request_id JOIN tuition_fee f ON"
                            + " f.id=p.tuition_fee_id WHERE t.outcome='RECONCILIATION' ORDER BY"
                            + " t.created_at DESC",
                        (rs, n) ->
                                new PaymentTransactionView(
                                        rs.getString(1),
                                        rs.getString(2),
                                        rs.getString(3),
                                        rs.getString(4),
                                        rs.getBigDecimal(5),
                                        rs.getString(6),
                                        rs.getString(7),
                                        rs.getTimestamp(8).toInstant(),
                                        true))
                .stream()
                .filter(
                        t ->
                                Access.can("FINANCE_STAFF", "STUDENT", t.studentId())
                                        || Access.can("FINANCE_STAFF", "SEMESTER", t.semesterId()))
                .toList();
    }

    private PaymentView payment(String id) {
        var found =
                jdbc.query(
                        "SELECT id,tuition_fee_id,amount,currency,status,expires_at FROM"
                                + " payment_request WHERE id=?",
                        (rs, n) ->
                                new PaymentView(
                                        rs.getString(1),
                                        rs.getString(2),
                                        rs.getBigDecimal(3),
                                        rs.getString(4),
                                        rs.getString(5),
                                        rs.getTimestamp(6).toInstant(),
                                        true),
                        id);
        if (found.isEmpty()) throw BusinessException.missing("Payment request");
        return found.getFirst();
    }
}
