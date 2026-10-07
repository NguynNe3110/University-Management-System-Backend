package com.university.academic.internal.web;

import com.university.academic.api.*;
import com.university.academic.internal.application.AcademicStructureService;
import com.university.shared.exception.BusinessException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
public class AcademicStructureController {
    private final AcademicStructureService service;

    public AcademicStructureController(AcademicStructureService service) {
        this.service = service;
    }

    public record Cohort(
            @NotBlank @Size(max = 32) String code, @Min(1900) @Max(9999) int admissionYear) {}

    public record AdministrativeClass(
            @NotBlank @Size(max = 32) String code,
            @NotBlank @Size(max = 36) String programId,
            @NotBlank @Size(max = 36) String cohortId) {}

    @PostMapping("/cohorts")
    public CohortView cohort(@Valid @RequestBody Cohort r) {
        return service.createCohort(r.code(), r.admissionYear());
    }

    @GetMapping("/cohorts")
    public List<CohortView> cohorts() {
        return service.cohorts();
    }

    @PostMapping("/administrative-classes")
    public AdministrativeClassView createClass(@Valid @RequestBody AdministrativeClass r) {
        return service.createClass(r.code(), r.programId(), r.cohortId());
    }

    @GetMapping("/administrative-classes")
    public List<AdministrativeClassView> classes() {
        return service.classes();
    }

    @GetMapping("/administrative-classes/{id}")
    public AdministrativeClassView read(@PathVariable String id) {
        return service.findAdministrativeClass(id)
                .orElseThrow(() -> BusinessException.missing("Administrative class"));
    }

    @PutMapping("/programs/{id}/curriculum")
    public CurriculumView curriculum(
            @PathVariable String id, @Valid @RequestBody CurriculumRequest r) {
        return service.curriculum(id, r);
    }

    @GetMapping("/programs/{id}/curriculum")
    public CurriculumView curriculum(
            @PathVariable String id, @RequestParam(required = false) Long version) {
        return version == null ? service.curriculum(id) : service.curriculumVersion(id, version);
    }
}
