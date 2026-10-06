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

    public TeachingController(TeachingJournal teachingJournal) {
        this.teachingJournal = teachingJournal;
    }

    @GetMapping("/logs/by-session/{timetableSessionId}")
    public ResponseEntity<TeachingLogView> getLogBySession(@PathVariable String timetableSessionId) {
        return teachingJournal.findByTimetableSessionId(timetableSessionId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
