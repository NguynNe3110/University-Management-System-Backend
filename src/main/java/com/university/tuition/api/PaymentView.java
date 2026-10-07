package com.university.tuition.api;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentView(
        String id,
        String tuitionFeeId,
        BigDecimal amount,
        String currency,
        String status,
        Instant expiresAt,
        boolean simulated) {}
