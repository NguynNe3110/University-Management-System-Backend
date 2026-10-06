package com.university.academic.internal.web;

import com.university.academic.api.AcademicCatalog;
import com.university.academic.api.ProgramView;
import com.university.academic.api.SemesterView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final AcademicCatalog academicCatalog;

    public AcademicController(AcademicCatalog academicCatalog) {
        this.academicCatalog = academicCatalog;
    }

    @GetMapping("/programs")
    public ResponseEntity<List<ProgramView>> getAllPrograms() {
        return ResponseEntity.ok(academicCatalog.findAllPrograms());
    }

    @GetMapping("/programs/{id}")
    public ResponseEntity<ProgramView> getProgramById(@PathVariable String id) {
        return academicCatalog.findProgramById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterView>> getAllSemesters() {
        return ResponseEntity.ok(academicCatalog.findAllSemesters());
    }

    @GetMapping("/semesters/{id}")
    public ResponseEntity<SemesterView> getSemesterById(@PathVariable String id) {
        return academicCatalog.findSemesterById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
