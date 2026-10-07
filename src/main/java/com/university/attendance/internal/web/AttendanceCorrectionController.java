package com.university.attendance.internal.web;

import com.university.attendance.api.AttendanceCorrectionView;
import com.university.attendance.internal.application.AttendanceCorrections;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceCorrectionController {
    private final AttendanceCorrections service;

    public AttendanceCorrectionController(AttendanceCorrections service) {
        this.service = service;
    }

    public record Proposal(
            @NotBlank @Size(max = 36) String studentId,
            @NotNull @Pattern(regexp = "PRESENT|ABSENT|EXCUSED") String requestedStatus,
            @NotBlank @Size(max = 2000) String reason) {}

    public record Decision(@NotNull Boolean approve, @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping("/sessions/{id}/corrections")
    public AttendanceCorrectionView propose(
            @PathVariable String id, @Valid @RequestBody Proposal r) {
        return service.propose(id, r.studentId(), r.requestedStatus(), r.reason());
    }

    @PostMapping("/corrections/{id}/decision")
    public AttendanceCorrectionView decide(
            @PathVariable String id, @Valid @RequestBody Decision r) {
        return service.decide(id, r.approve(), r.reason());
    }
}
