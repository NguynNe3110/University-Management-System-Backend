package com.university.lecturer.internal.web;

import com.university.lecturer.api.*;
import com.university.lecturer.internal.application.LecturerCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerWriteController {
    private final LecturerCommands commands;

    public LecturerWriteController(LecturerCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<LecturerProfileView> create(
            @Valid @RequestBody LecturerProfileRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/lecturers/" + v.lecturerCode())).body(v);
    }

    @PutMapping("/{id}")
    public LecturerProfileView update(
            @PathVariable String id, @Valid @RequestBody LecturerProfileRequest r) {
        return commands.update(id, r);
    }
}
