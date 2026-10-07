package com.university.student.internal.web;

import com.university.student.api.StudentDirectory;
import com.university.student.api.StudentProfileView;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentDirectory studentDirectory;
    private final com.university.academic.api.AcademicCatalog academic;

    public StudentController(
            StudentDirectory studentDirectory,
            com.university.academic.api.AcademicCatalog academic) {
        this.studentDirectory = studentDirectory;
        this.academic = academic;
    }

    private boolean staff(StudentProfileView s) {
        var p = academic.findProgramById(s.programId()).orElseThrow();
        return com.university.shared.security.Access.can(
                        "ACADEMIC_STAFF", "DEPARTMENT", p.departmentId())
                || com.university.shared.security.Access.can("ACADEMIC_STAFF", "STUDENT", s.id());
    }

    @GetMapping
    public ResponseEntity<List<StudentProfileView>> getAllStudents() {
        return ResponseEntity.ok(
                studentDirectory.findAllStudents().stream().filter(s -> staff(s)).toList());
    }

    @GetMapping("/{studentCode}")
    public ResponseEntity<StudentProfileView> getStudentByCode(@PathVariable String studentCode) {
        return studentDirectory
                .findByStudentCode(studentCode)
                .map(
                        s -> {
                            if (!com.university.shared.security.Access.self("STUDENT", s.id())
                                    && !staff(s))
                                throw new org.springframework.security.access.AccessDeniedException(
                                        "Student outside assigned scope");
                            return ResponseEntity.ok(s);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
