package com.university.timetable.internal.web;

import com.university.timetable.api.ScheduleChangeView;
import com.university.timetable.internal.application.ScheduleChanges;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/timetable")
public class ScheduleChangeController {
    private final ScheduleChanges service;

    public ScheduleChangeController(ScheduleChanges service) {
        this.service = service;
    }

    public record Proposal(
            @NotNull Boolean cancel,
            @Size(max = 36) String roomId,
            Instant startsAt,
            Instant endsAt,
            @NotBlank @Size(max = 2000) String reason) {}

    public record Decision(@NotNull Boolean approve, @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping("/sessions/{id}/changes")
    public ScheduleChangeView propose(@PathVariable String id, @Valid @RequestBody Proposal r) {
        return service.propose(id, r.cancel(), r.roomId(), r.startsAt(), r.endsAt(), r.reason());
    }

    @GetMapping("/changes/{id}")
    public ScheduleChangeView read(@PathVariable String id) {
        return service.read(id);
    }

    @PostMapping("/changes/{id}/decision")
    public ScheduleChangeView decide(@PathVariable String id, @Valid @RequestBody Decision r) {
        return service.decide(id, r.approve(), r.reason());
    }
}
