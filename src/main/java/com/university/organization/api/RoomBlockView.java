package com.university.organization.api;

import java.time.Instant;

public record RoomBlockView(
        String id, String roomId, Instant startsAt, Instant endsAt, String reason) {}
