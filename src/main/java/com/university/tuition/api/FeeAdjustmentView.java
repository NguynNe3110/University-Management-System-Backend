package com.university.tuition.api;

import java.math.BigDecimal;

public record FeeAdjustmentView(
        String id,
        String tuitionFeeId,
        BigDecimal delta,
        String reason,
        String proposedBy,
        String status) {}
