package com.university.teachingclass.internal.web;

import com.university.teachingclass.api.*;
import com.university.teachingclass.internal.application.ClassCommands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/teaching-classes")
public class ClassWriteController {
    private final ClassCommands commands;

    public ClassWriteController(ClassCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<TeachingClassView> create(@Valid @RequestBody ClassRequest r) {
        var c = commands.create(r);
        return ResponseEntity.created(URI.create("/api/teaching-classes/" + c.id())).body(c);
    }

    public record Version(@Min(0) long version) {}

    @PostMapping("/{id}/open")
    public TeachingClassView open(@PathVariable String id, @Valid @RequestBody Version r) {
        return commands.open(id, r.version());
    }
}
