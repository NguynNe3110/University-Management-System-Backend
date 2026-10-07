package com.university.organization.internal.web;

import com.university.organization.api.*;
import com.university.organization.internal.application.DepartmentCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/organization/departments")
public class DepartmentWriteController {
    private final DepartmentCommands commands;

    public DepartmentWriteController(DepartmentCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<DepartmentView> create(@Valid @RequestBody DepartmentRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/organization/departments/" + v.id()))
                .body(v);
    }

    @PutMapping("/{id}")
    public DepartmentView update(@PathVariable String id, @Valid @RequestBody DepartmentRequest r) {
        return commands.update(id, r);
    }
}
