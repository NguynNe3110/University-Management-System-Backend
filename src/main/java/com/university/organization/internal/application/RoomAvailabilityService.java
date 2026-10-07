package com.university.organization.internal.application;

import com.university.organization.api.*;
import com.university.organization.internal.persistence.RoomRepository;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class RoomAvailabilityService implements RoomAvailability {
    private final RoomRepository rooms;
    private final RoomReservationCheck reservations;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;

    public RoomAvailabilityService(
            RoomRepository rooms,
            RoomReservationCheck reservations,
            JdbcTemplate jdbc,
            TechnicalAudit audit) {
        this.rooms = rooms;
        this.reservations = reservations;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireAvailable(String room, Instant start, Instant end) {
        rooms.lockById(room).orElseThrow(() -> BusinessException.missing("Room"));
        if (jdbc.queryForObject(
                        "SELECT count(*) FROM room_block WHERE room_id=? AND starts_at<? AND"
                            + " ends_at>?",
                        Integer.class,
                        room,
                        Timestamp.from(end),
                        Timestamp.from(start))
                > 0) throw BusinessException.conflict("Room is unavailable during this interval");
    }

    public RoomBlockView block(String room, Instant start, Instant end, String reason) {
        Access.require("ROOM_MANAGER", "ROOM", room);
        if (!end.isAfter(start)) throw new IllegalArgumentException("Invalid room block interval");
        rooms.lockById(room).orElseThrow(() -> BusinessException.missing("Room"));
        if (reservations.reserved(room, start, end))
            throw BusinessException.conflict(
                    "Room already has a published reservation; change the timetable first");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO room_block(id,room_id,starts_at,ends_at,reason) VALUES (?,?,?,?,?)",
                id,
                room,
                Timestamp.from(start),
                Timestamp.from(end),
                reason);
        audit.record("organization", id, "BLOCK_ROOM", reason);
        return new RoomBlockView(id, room, start, end, reason);
    }

    @Transactional(readOnly = true)
    public List<RoomBlockView> blocks(String room) {
        return jdbc.query(
                "SELECT * FROM room_block WHERE room_id=? ORDER BY starts_at",
                (rs, n) ->
                        new RoomBlockView(
                                rs.getString("id"),
                                rs.getString("room_id"),
                                rs.getTimestamp("starts_at").toInstant(),
                                rs.getTimestamp("ends_at").toInstant(),
                                rs.getString("reason")),
                room);
    }

    public void unblock(String room, String id) {
        Access.require("ROOM_MANAGER", "ROOM", room);
        rooms.lockById(room).orElseThrow(() -> BusinessException.missing("Room"));
        if (jdbc.update("DELETE FROM room_block WHERE id=? AND room_id=?", id, room) == 0)
            throw BusinessException.missing("Room block");
        audit.record("organization", id, "UNBLOCK_ROOM", room);
    }
}
