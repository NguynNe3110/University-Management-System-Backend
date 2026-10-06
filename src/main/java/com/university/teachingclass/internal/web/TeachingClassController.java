package com.university.teachingclass.internal.web;

import com.university.teachingclass.api.TeachingClassDirectory;
import com.university.teachingclass.api.TeachingClassView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teaching-classes")
public class TeachingClassController {

    private final TeachingClassDirectory teachingClassDirectory;

    public TeachingClassController(TeachingClassDirectory teachingClassDirectory) {
        this.teachingClassDirectory = teachingClassDirectory;
    }

    @GetMapping
    public ResponseEntity<List<TeachingClassView>> getClasses(@RequestParam(required = false) String semesterId) {
        if (semesterId != null && !semesterId.isBlank()) {
            return ResponseEntity.ok(teachingClassDirectory.findClassesBySemester(semesterId));
        }
        return ResponseEntity.ok(teachingClassDirectory.findAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeachingClassView> getClassById(@PathVariable String id) {
        return teachingClassDirectory.findClassById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
