package com.university.organization.api;

import java.time.Instant;

public interface RoomAvailability {
    void requireAvailable(String room, Instant start, Instant end);
}
