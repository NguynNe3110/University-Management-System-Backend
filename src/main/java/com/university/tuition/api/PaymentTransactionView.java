package com.university.tuition.api;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentTransactionView(
        String transactionId,
        String paymentRequestId,
        String studentId,
        String semesterId,
        BigDecimal amount,
        String currency,
        String outcome,
        Instant receivedAt,
        boolean simulated) {}
