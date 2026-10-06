package com.university.grade.internal.web;

import com.university.grade.api.GradeBook;
import com.university.grade.api.StudentGradeView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeBook gradeBook;

    public GradeController(GradeBook gradeBook) {
        this.gradeBook = gradeBook;
    }

    @GetMapping("/by-class")
    public ResponseEntity<StudentGradeView> getGradeByClass(@RequestParam String studentId, @RequestParam String teachingClassId) {
        return gradeBook.findGrade(studentId, teachingClassId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<StudentGradeView>> getGradesByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(gradeBook.findGradesByStudent(studentId));
    }
}
