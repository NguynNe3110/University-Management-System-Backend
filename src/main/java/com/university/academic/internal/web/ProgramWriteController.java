package com.university.academic.internal.web;

import com.university.academic.api.*;
import com.university.academic.internal.application.ProgramCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/academic/programs")
public class ProgramWriteController {
    private final ProgramCommands commands;

    public ProgramWriteController(ProgramCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<ProgramView> create(@Valid @RequestBody ProgramRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/academic/programs/" + v.id())).body(v);
    }

    @PutMapping("/{id}")
    public ProgramView update(@PathVariable String id, @Valid @RequestBody ProgramRequest r) {
        return commands.update(id, r);
    }
}
