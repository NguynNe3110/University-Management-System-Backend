package com.university.organization.api;

import java.time.Instant;

public interface RoomReservationCheck {
    boolean reserved(String room, Instant start, Instant end);
}
