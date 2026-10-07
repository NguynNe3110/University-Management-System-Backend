package com.university.grade.internal.web;

import com.university.grade.api.*;
import com.university.grade.internal.application.GradeCorrections;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
public class GradeCorrectionController {
    private final GradeCorrections service;

    public GradeCorrectionController(GradeCorrections service) {
        this.service = service;
    }

    public record Proposal(
            @NotNull @Valid GradeBatchRequest.Entry scores,
            @NotBlank @Size(max = 2000) String reason) {}

    public record Decision(@NotNull Boolean approve, @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping("/classes/{id}/corrections")
    public GradeCorrectionView propose(@PathVariable String id, @Valid @RequestBody Proposal r) {
        return service.propose(id, r.scores(), r.reason());
    }

    @GetMapping("/corrections/{id}")
    public GradeCorrectionView read(@PathVariable String id) {
        return service.read(id);
    }

    @PostMapping("/corrections/{id}/decision")
    public GradeCorrectionView decide(@PathVariable String id, @Valid @RequestBody Decision r) {
        return service.decide(id, r.approve(), r.reason());
    }
}
