package com.university.tuition.api;

import java.math.BigDecimal;

public record TuitionFeeView(
    String id,
    String studentId,
    String semesterId,
    BigDecimal amountDue,
    BigDecimal amountPaid,
    String status
) {}
