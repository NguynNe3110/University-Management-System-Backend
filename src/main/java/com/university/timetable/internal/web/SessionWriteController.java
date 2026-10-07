package com.university.timetable.internal.web;

import com.university.timetable.api.*;
import com.university.timetable.internal.application.SessionCommands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/timetable/sessions")
public class SessionWriteController {
    private final SessionCommands commands;

    public SessionWriteController(SessionCommands commands) {
        this.commands = commands;
    }

    @PostMapping
    public ResponseEntity<TimetableSessionView> create(@Valid @RequestBody SessionRequest r) {
        var s = commands.create(r);
        return ResponseEntity.created(URI.create("/api/timetable/sessions/" + s.id())).body(s);
    }

    public record Version(@Min(0) long version) {}

    @PostMapping("/{id}/publish")
    public TimetableSessionView publish(@PathVariable String id, @Valid @RequestBody Version r) {
        return commands.publish(id, r.version());
    }

    @PostMapping("/{id}/held")
    public TimetableSessionView held(@PathVariable String id, @Valid @RequestBody Version r) {
        return commands.held(id, r.version());
    }
}
