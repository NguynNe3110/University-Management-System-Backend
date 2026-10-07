package com.university.enrollment.internal.web;

import com.university.enrollment.api.EnrollmentRegistry;
import com.university.enrollment.api.EnrollmentView;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentRegistry enrollmentRegistry;
    private final com.university.teachingclass.api.ClassAccess access;

    public EnrollmentController(
            EnrollmentRegistry enrollmentRegistry,
            com.university.teachingclass.api.ClassAccess access) {
        this.enrollmentRegistry = enrollmentRegistry;
        this.access = access;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentView> getEnrollmentById(@PathVariable String id) {
        return enrollmentRegistry
                .findById(id)
                .map(
                        e -> {
                            if (!com.university.shared.security.Access.self(
                                            "STUDENT", e.studentId())
                                    && !access.canRead(e.teachingClassId()))
                                throw new org.springframework.security.access.AccessDeniedException(
                                        "Enrollment outside assigned scope");
                            return ResponseEntity.ok(e);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<EnrollmentView>> getByStudent(@PathVariable String studentId) {
        if (!com.university.shared.security.Access.self("STUDENT", studentId))
            com.university.shared.security.Access.require("ACADEMIC_STAFF", "STUDENT", studentId);
        return ResponseEntity.ok(enrollmentRegistry.findByStudentId(studentId));
    }

    @GetMapping("/by-class/{teachingClassId}")
    public ResponseEntity<List<EnrollmentView>> getByClass(@PathVariable String teachingClassId) {
        if (!access.canRead(teachingClassId))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Roster outside assigned scope");
        return ResponseEntity.ok(enrollmentRegistry.findByTeachingClassId(teachingClassId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentView> enroll(
            @jakarta.validation.Valid @RequestBody EnrollmentRequest request) {
        EnrollmentView view =
                enrollmentRegistry.enroll(
                        request.studentId(), request.teachingClassId(), request.windowId());
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EnrollmentView> cancel(@PathVariable String id) {
        return ResponseEntity.ok(enrollmentRegistry.cancel(id));
    }
}
