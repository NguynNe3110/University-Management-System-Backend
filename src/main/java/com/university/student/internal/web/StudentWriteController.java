package com.university.student.internal.web;

import com.university.student.api.*;
import com.university.student.internal.application.StudentCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/students")
public class StudentWriteController {
    private final StudentCommands commands;

    public StudentWriteController(StudentCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<StudentProfileView> create(@Valid @RequestBody StudentProfileRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/students/" + v.studentCode())).body(v);
    }

    @PutMapping("/{id}")
    public StudentProfileView update(
            @PathVariable String id, @Valid @RequestBody StudentProfileRequest r) {
        return commands.update(id, r);
    }
}
