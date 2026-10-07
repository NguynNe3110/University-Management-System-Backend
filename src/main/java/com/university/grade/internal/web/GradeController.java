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
    private final com.university.teachingclass.api.ClassAccess access;

    public GradeController(
            GradeBook gradeBook, com.university.teachingclass.api.ClassAccess access) {
        this.gradeBook = gradeBook;
        this.access = access;
    }

    @GetMapping("/by-class")
    public ResponseEntity<StudentGradeView> getGradeByClass(
            @RequestParam String studentId, @RequestParam String teachingClassId) {
        if (!com.university.shared.security.Access.self("STUDENT", studentId)
                && !access.canRead(teachingClassId))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Grade outside assigned scope");
        return gradeBook
                .findGrade(studentId, teachingClassId)
                .filter(
                        g ->
                                !com.university.shared.security.Access.self("STUDENT", studentId)
                                        || g.status().equals("PUBLISHED"))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<StudentGradeView>> getGradesByStudent(
            @PathVariable String studentId) {
        com.university.shared.security.Access.requireSelf("STUDENT", studentId);
        return ResponseEntity.ok(
                gradeBook.findGradesByStudent(studentId).stream()
                        .filter(g -> g.status().equals("PUBLISHED"))
                        .toList());
    }
}
