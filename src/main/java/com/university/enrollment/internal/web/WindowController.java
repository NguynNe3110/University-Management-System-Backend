package com.university.enrollment.internal.web;

import com.university.enrollment.api.*;
import com.university.enrollment.internal.application.EnrollmentRegistryImpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class WindowController {
    private final EnrollmentRegistryImpl service;

    public WindowController(EnrollmentRegistryImpl service) {
        this.service = service;
    }

    @GetMapping("/windows")
    public List<WindowView> windows() {
        return service.listWindows();
    }

    @PostMapping("/windows")
    public ResponseEntity<WindowView> create(@Valid @RequestBody WindowRequest r) {
        var w = service.createWindow(r);
        return ResponseEntity.created(URI.create("/api/enrollments/windows")).body(w);
    }

    public record Transfer(
            @NotBlank @Size(max = 36) String teachingClassId,
            @NotBlank @Size(max = 36) String windowId) {}

    @PostMapping("/{id}/transfer")
    public EnrollmentView transfer(@PathVariable String id, @Valid @RequestBody Transfer r) {
        return service.transfer(id, r.teachingClassId(), r.windowId());
    }
}
