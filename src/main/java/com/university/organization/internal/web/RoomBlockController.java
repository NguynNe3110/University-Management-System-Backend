package com.university.organization.internal.web;

import com.university.organization.api.RoomBlockView;
import com.university.organization.internal.application.RoomAvailabilityService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/organization/rooms/{roomId}/blocks")
public class RoomBlockController {
    private final RoomAvailabilityService service;

    public RoomBlockController(RoomAvailabilityService service) {
        this.service = service;
    }

    public record Block(
            @NotNull Instant startsAt,
            @NotNull Instant endsAt,
            @NotBlank @Size(max = 2000) String reason) {}

    @PostMapping
    public RoomBlockView block(@PathVariable String roomId, @Valid @RequestBody Block r) {
        return service.block(roomId, r.startsAt(), r.endsAt(), r.reason());
    }

    @GetMapping
    public List<RoomBlockView> list(@PathVariable String roomId) {
        return service.blocks(roomId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable String roomId, @PathVariable String id) {
        service.unblock(roomId, id);
        return ResponseEntity.noContent().build();
    }
}
