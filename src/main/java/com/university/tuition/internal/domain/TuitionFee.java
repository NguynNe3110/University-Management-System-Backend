package com.university.tuition.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tuition_fee")
public class TuitionFee {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "semester_id", length = 36, nullable = false)
    private String semesterId;

    @Column(name = "amount_due", precision = 12, scale = 2, nullable = false)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", precision = 12, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TuitionFee() {}

    public TuitionFee(String id, String studentId, String semesterId, BigDecimal amountDue, BigDecimal amountPaid, String status) {
        this.id = id;
        this.studentId = studentId;
        this.semesterId = semesterId;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getSemesterId() { return semesterId; }
    public BigDecimal getAmountDue() { return amountDue; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
