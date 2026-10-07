package com.university.grade.internal.web;

import com.university.grade.api.*;
import com.university.grade.internal.application.GradeCommands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades/classes")
public class GradeWriteController {
    private final GradeCommands commands;

    public GradeWriteController(GradeCommands commands) {
        this.commands = commands;
    }

    public record Version(@Min(0) long version) {}

    @GetMapping("/{id}")
    public GradeBatchView read(@PathVariable String id) {
        return commands.read(id);
    }

    @PutMapping("/{id}")
    public GradeBatchView save(@PathVariable String id, @Valid @RequestBody GradeBatchRequest r) {
        return commands.save(id, r);
    }

    @PostMapping("/{id}/submit")
    public GradeBatchView submit(@PathVariable String id, @Valid @RequestBody Version r) {
        return commands.submit(id, r.version());
    }

    @PostMapping("/{id}/publish")
    public GradeBatchView publish(@PathVariable String id, @Valid @RequestBody Version r) {
        return commands.publish(id, r.version());
    }
}
