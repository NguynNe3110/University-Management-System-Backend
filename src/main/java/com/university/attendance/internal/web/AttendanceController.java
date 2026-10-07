package com.university.attendance.internal.web;

import com.university.attendance.api.AttendanceRecordView;
import com.university.attendance.api.AttendanceRegistry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRegistry attendanceRegistry;
    private final com.university.attendance.internal.application.AttendanceCommands commands;

    public AttendanceController(
            AttendanceRegistry attendanceRegistry,
            com.university.attendance.internal.application.AttendanceCommands commands) {
        this.attendanceRegistry = attendanceRegistry;
        this.commands = commands;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceRecordView> getRecordById(@PathVariable String id) {
        return attendanceRegistry
                .findById(id)
                .map(
                        r -> {
                            if (!com.university.shared.security.Access.self(
                                    "STUDENT", r.studentId()))
                                commands.requireRosterRead(r.attendanceSessionId());
                            return ResponseEntity.ok(r);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-session/{sessionId}")
    public ResponseEntity<List<AttendanceRecordView>> getBySessionId(
            @PathVariable String sessionId) {
        commands.requireRosterRead(sessionId);
        return ResponseEntity.ok(attendanceRegistry.findBySessionId(sessionId));
    }
}
