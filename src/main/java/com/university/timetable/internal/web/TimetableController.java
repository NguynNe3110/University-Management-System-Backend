package com.university.timetable.internal.web;

import com.university.timetable.api.TimetableCatalog;
import com.university.timetable.api.TimetableSessionView;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableCatalog timetableCatalog;
    private final com.university.teachingclass.api.ClassAccess access;

    public TimetableController(
            TimetableCatalog timetableCatalog,
            com.university.teachingclass.api.ClassAccess access) {
        this.timetableCatalog = timetableCatalog;
        this.access = access;
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<TimetableSessionView> getSessionById(@PathVariable String id) {
        return timetableCatalog
                .findSessionById(id)
                .filter(s -> !s.status().equals("DRAFT") || access.canRead(s.teachingClassId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<TimetableSessionView>> getSessionsByClass(
            @RequestParam String teachingClassId) {
        return ResponseEntity.ok(
                timetableCatalog.findSessionsByTeachingClassId(teachingClassId).stream()
                        .filter(s -> !s.status().equals("DRAFT") || access.canRead(teachingClassId))
                        .toList());
    }
}
