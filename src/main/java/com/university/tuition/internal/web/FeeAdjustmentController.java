package com.university.tuition.internal.web;

import com.university.tuition.api.*;
import com.university.tuition.internal.application.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tuition")
public class FeeAdjustmentController {
    private final FeeAdjustments service;
    private final PaymentService payments;

    public FeeAdjustmentController(FeeAdjustments service, PaymentService payments) {
        this.service = service;
        this.payments = payments;
    }

    public record Proposal(
            @NotNull @Digits(integer = 10, fraction = 2) BigDecimal delta,
            @NotBlank @Size(max = 2000) String reason) {}

    public record Decision(@NotNull Boolean approve, @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping("/fees/{id}/adjustments")
    public FeeAdjustmentView propose(@PathVariable String id, @Valid @RequestBody Proposal r) {
        return service.propose(id, r.delta(), r.reason());
    }

    @GetMapping("/adjustments/{id}")
    public FeeAdjustmentView read(@PathVariable String id) {
        return service.read(id);
    }

    @PostMapping("/adjustments/{id}/decision")
    public FeeAdjustmentView decide(@PathVariable String id, @Valid @RequestBody Decision r) {
        return service.decide(id, r.approve(), r.reason());
    }

    @GetMapping("/reconciliation")
    public List<PaymentTransactionView> reconciliation() {
        return payments.reconciliation();
    }
}
