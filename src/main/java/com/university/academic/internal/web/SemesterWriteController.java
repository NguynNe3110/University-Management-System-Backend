package com.university.academic.internal.web;

import com.university.academic.api.*;
import com.university.academic.internal.application.SemesterCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/academic/semesters")
public class SemesterWriteController {
    private final SemesterCommands commands;

    public SemesterWriteController(SemesterCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<SemesterView> create(@Valid @RequestBody SemesterRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/academic/semesters/" + v.id())).body(v);
    }

    @PutMapping("/{id}")
    public SemesterView update(@PathVariable String id, @Valid @RequestBody SemesterRequest r) {
        return commands.update(id, r);
    }
}
