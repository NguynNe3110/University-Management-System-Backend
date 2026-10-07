package com.university.teaching.internal.web;

import com.university.teaching.api.TeachingJournal;
import com.university.teaching.api.TeachingLogView;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teaching")
public class TeachingController {

    private final TeachingJournal teachingJournal;
    private final com.university.timetable.api.TimetableCatalog timetable;
    private final com.university.teachingclass.api.ClassAccess access;

    public TeachingController(
            TeachingJournal teachingJournal,
            com.university.timetable.api.TimetableCatalog timetable,
            com.university.teachingclass.api.ClassAccess access) {
        this.teachingJournal = teachingJournal;
        this.timetable = timetable;
        this.access = access;
    }

    @GetMapping("/logs/by-session/{timetableSessionId}")
    public ResponseEntity<TeachingLogView> getLogBySession(
            @PathVariable String timetableSessionId) {
        var t =
                timetable
                        .findSessionById(timetableSessionId)
                        .orElseThrow(
                                () ->
                                        com.university.shared.exception.BusinessException.missing(
                                                "Session"));
        if (!access.canRead(t.teachingClassId()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Teaching log outside assigned scope");
        return teachingJournal
                .findByTimetableSessionId(timetableSessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
