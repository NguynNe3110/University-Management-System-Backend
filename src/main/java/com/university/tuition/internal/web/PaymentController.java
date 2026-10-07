package com.university.tuition.internal.web;

import com.university.tuition.api.*;
import com.university.tuition.internal.application.PaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/tuition")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    public record Issue(
            @NotBlank @Size(max = 36) String studentId,
            @NotBlank @Size(max = 36) String semesterId) {}

    public record Request(
            @NotBlank @Size(max = 36) String tuitionFeeId,
            @NotBlank @Size(max = 64) String requestKey,
            @NotNull Instant expiresAt) {}

    public record Simulation(
            @NotBlank @Size(max = 64) String transactionId,
            @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
            @NotBlank @Size(max = 3) String currency) {}

    @PostMapping("/issue")
    public TuitionFeeView issue(@Valid @RequestBody Issue r) {
        return service.issue(r.studentId(), r.semesterId());
    }

    @PostMapping("/payments")
    public PaymentView request(@Valid @RequestBody Request r) {
        return service.request(r.tuitionFeeId(), r.requestKey(), r.expiresAt());
    }

    @GetMapping("/payments/{id}")
    public PaymentView read(@PathVariable String id) {
        return service.read(id);
    }

    @PostMapping("/payments/{id}/simulate")
    public PaymentView simulate(@PathVariable String id, @Valid @RequestBody Simulation r) {
        return service.simulate(id, r.transactionId(), r.amount(), r.currency());
    }
}
