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

    public EnrollmentController(EnrollmentRegistry enrollmentRegistry) {
        this.enrollmentRegistry = enrollmentRegistry;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentView> getEnrollmentById(@PathVariable String id) {
        return enrollmentRegistry.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<EnrollmentView>> getByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(enrollmentRegistry.findByStudentId(studentId));
    }

    @GetMapping("/by-class/{teachingClassId}")
    public ResponseEntity<List<EnrollmentView>> getByClass(@PathVariable String teachingClassId) {
        return ResponseEntity.ok(enrollmentRegistry.findByTeachingClassId(teachingClassId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentView> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentView view = enrollmentRegistry.enroll(request.studentId(), request.teachingClassId());
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EnrollmentView> cancel(@PathVariable String id) {
        return ResponseEntity.ok(enrollmentRegistry.cancel(id));
    }
}
