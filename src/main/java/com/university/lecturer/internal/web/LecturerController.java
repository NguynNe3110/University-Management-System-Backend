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

    private boolean staff(LecturerProfileView l) {
        return com.university.shared.security.Access.can(
                        "ACADEMIC_STAFF", "DEPARTMENT", l.departmentId())
                || com.university.shared.security.Access.can(
                        "FACULTY_STAFF", "DEPARTMENT", l.departmentId());
    }

    @GetMapping
    public ResponseEntity<List<LecturerProfileView>> getAllLecturers() {
        return ResponseEntity.ok(
                lecturerDirectory.findAllLecturers().stream().filter(l -> staff(l)).toList());
    }

    @GetMapping("/{lecturerCode}")
    public ResponseEntity<LecturerProfileView> getLecturerByCode(
            @PathVariable String lecturerCode) {
        return lecturerDirectory
                .findByLecturerCode(lecturerCode)
                .map(
                        l -> {
                            if (!com.university.shared.security.Access.self("LECTURER", l.id())
                                    && !staff(l))
                                throw new org.springframework.security.access.AccessDeniedException(
                                        "Lecturer outside assigned scope");
                            return ResponseEntity.ok(l);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
