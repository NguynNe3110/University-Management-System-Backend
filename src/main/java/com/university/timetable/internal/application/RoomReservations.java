package com.university.timetable.internal.application;

import com.university.organization.api.RoomReservationCheck;
import com.university.timetable.internal.persistence.TimetableSessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class RoomReservations implements RoomReservationCheck {
    private final TimetableSessionRepository sessions;

    public RoomReservations(TimetableSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean reserved(String room, Instant start, Instant end) {
        return sessions.findAll().stream()
                .anyMatch(
                        s ->
                                room.equals(s.getRoomId())
                                        && !s.getStatus().equals("CANCELLED")
                                        && !s.getStatus().equals("DRAFT")
                                        && s.getStartsAt() != null
                                        && start.isBefore(s.getEndsAt())
                                        && end.isAfter(s.getStartsAt()));
    }
}
