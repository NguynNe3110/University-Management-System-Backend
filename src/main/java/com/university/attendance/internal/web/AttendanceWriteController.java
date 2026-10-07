package com.university.attendance.internal.web;

import com.university.attendance.api.*;
import com.university.attendance.internal.application.AttendanceCommands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/attendance/sessions")
public class AttendanceWriteController {
    private final AttendanceCommands commands;

    public AttendanceWriteController(AttendanceCommands commands) {
        this.commands = commands;
    }

    public record Open(
            @NotBlank @Size(max = 36) String timetableSessionId, @NotNull Instant expiresAt) {}

    public record CheckIn(@NotBlank @Size(max = 255) String qrToken) {}

    @PostMapping
    public ResponseEntity<AttendanceSessionView> open(@Valid @RequestBody Open r) {
        var s = commands.open(r.timetableSessionId(), r.expiresAt());
        return ResponseEntity.created(URI.create("/api/attendance/sessions/" + s.id())).body(s);
    }

    @GetMapping("/{id}")
    public AttendanceSessionView session(@PathVariable String id) {
        return commands.session(id);
    }

    @PostMapping("/{id}/close")
    public AttendanceSessionView close(@PathVariable String id) {
        return commands.close(id);
    }

    @PostMapping("/{id}/check-in")
    public AttendanceRecordView checkIn(@PathVariable String id, @Valid @RequestBody CheckIn r) {
        return commands.checkIn(id, r.qrToken());
    }
}
