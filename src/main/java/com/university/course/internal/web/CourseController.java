package com.university.course.internal.web;

import com.university.course.api.CourseView;
import com.university.course.internal.application.CourseService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@Validated
@PreAuthorize("isAuthenticated()")
public class CourseController {
    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public CoursePage list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var result = service.list(page, size);
        return new CoursePage(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @GetMapping("/{id}")
    public CourseView get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize(
            "hasRole('ACADEMIC_STAFF') and"
                + " T(com.university.shared.security.Access).can('ACADEMIC_STAFF','GLOBAL','*')")
    public ResponseEntity<CourseView> create(@Valid @RequestBody CreateCourse request) {
        var course = service.create(request.code(), request.name(), request.credits());
        return ResponseEntity.created(URI.create("/api/v1/courses/" + course.id())).body(course);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasRole('ACADEMIC_STAFF') and"
                + " T(com.university.shared.security.Access).can('ACADEMIC_STAFF','GLOBAL','*')")
    public CourseView update(@PathVariable UUID id, @Valid @RequestBody UpdateCourse request) {
        return service.update(id, request.name(), request.credits(), request.version());
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize(
            "hasRole('ACADEMIC_STAFF') and"
                + " T(com.university.shared.security.Access).can('ACADEMIC_STAFF','GLOBAL','*')")
    public CourseView archive(@PathVariable UUID id, @Valid @RequestBody ArchiveCourse request) {
        return service.archive(id, request.version());
    }

    public record CreateCourse(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 200) String name,
            @NotNull @Min(1) @Max(30) Integer credits) {}

    public record UpdateCourse(
            @NotBlank @Size(max = 200) String name,
            @NotNull @Min(1) @Max(30) Integer credits,
            @NotNull @PositiveOrZero Long version) {}

    public record ArchiveCourse(@NotNull @PositiveOrZero Long version) {}

    public record CoursePage(List<CourseView> content, int page, int size, long totalElements) {}
}
