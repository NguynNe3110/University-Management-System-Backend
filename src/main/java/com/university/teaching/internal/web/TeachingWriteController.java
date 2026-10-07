package com.university.teaching.internal.web;

import com.university.teaching.api.TeachingLogView;
import com.university.teaching.internal.application.TeachingCommands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teaching/logs")
public class TeachingWriteController {
    private final TeachingCommands commands;

    public TeachingWriteController(TeachingCommands commands) {
        this.commands = commands;
    }

    public record Submit(
            @NotBlank @Size(max = 36) String timetableSessionId,
            @Min(1) int actualHours,
            @NotBlank @Size(max = 10000) String contentSummary) {}

    public record Decision(@NotNull Boolean approve, @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping
    public TeachingLogView submit(@Valid @RequestBody Submit r) {
        return commands.submit(r.timetableSessionId(), r.actualHours(), r.contentSummary());
    }

    @PostMapping("/{id}/decision")
    public TeachingLogView decide(@PathVariable String id, @Valid @RequestBody Decision r) {
        return commands.decide(id, r.approve(), r.reason());
    }
}
