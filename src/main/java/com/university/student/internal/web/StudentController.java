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

    public StudentController(StudentDirectory studentDirectory) {
        this.studentDirectory = studentDirectory;
    }

    @GetMapping
    public ResponseEntity<List<StudentProfileView>> getAllStudents() {
        return ResponseEntity.ok(studentDirectory.findAllStudents());
    }

    @GetMapping("/{studentCode}")
    public ResponseEntity<StudentProfileView> getStudentByCode(@PathVariable String studentCode) {
        return studentDirectory.findByStudentCode(studentCode)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
