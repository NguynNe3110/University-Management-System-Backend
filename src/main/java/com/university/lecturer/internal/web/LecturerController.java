package com.university.lecturer.internal.web;

import com.university.lecturer.api.LecturerDirectory;
import com.university.lecturer.api.LecturerProfileView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerController {

    private final LecturerDirectory lecturerDirectory;

    public LecturerController(LecturerDirectory lecturerDirectory) {
        this.lecturerDirectory = lecturerDirectory;
    }

    @GetMapping
    public ResponseEntity<List<LecturerProfileView>> getAllLecturers() {
        return ResponseEntity.ok(lecturerDirectory.findAllLecturers());
    }

    @GetMapping("/{lecturerCode}")
    public ResponseEntity<LecturerProfileView> getLecturerByCode(@PathVariable String lecturerCode) {
        return lecturerDirectory.findByLecturerCode(lecturerCode)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
