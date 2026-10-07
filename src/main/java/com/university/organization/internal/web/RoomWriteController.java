package com.university.organization.internal.web;

import com.university.organization.api.*;
import com.university.organization.internal.application.RoomCommands;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/organization/rooms")
public class RoomWriteController {
    private final RoomCommands commands;

    public RoomWriteController(RoomCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<RoomView> create(@Valid @RequestBody RoomRequest r) {
        var v = commands.create(r);
        return ResponseEntity.created(URI.create("/api/organization/rooms/" + v.id())).body(v);
    }

    @PutMapping("/{id}")
    public RoomView update(@PathVariable String id, @Valid @RequestBody RoomRequest r) {
        return commands.update(id, r);
    }
}
