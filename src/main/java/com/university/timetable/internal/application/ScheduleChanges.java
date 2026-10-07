package com.university.timetable.internal.application;

import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.*;
import com.university.timetable.api.*;
import com.university.timetable.internal.persistence.TimetableSessionRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class ScheduleChanges {
    private final TimetableSessionRepository sessions;
    private final SessionCommands commands;
    private final TimetableCatalog timetable;
    private final TeachingClassDirectory classes;
    private final ClassAccess access;
    private final OrganizationDirectory organization;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;
    private final ApplicationEventPublisher events;

    public ScheduleChanges(
            TimetableSessionRepository sessions,
            SessionCommands commands,
            TimetableCatalog timetable,
            TeachingClassDirectory classes,
            ClassAccess access,
            OrganizationDirectory organization,
            JdbcTemplate jdbc,
            TechnicalAudit audit,
            ApplicationEventPublisher events) {
        this.sessions = sessions;
        this.commands = commands;
        this.timetable = timetable;
        this.classes = classes;
        this.access = access;
        this.organization = organization;
        this.jdbc = jdbc;
        this.audit = audit;
        this.events = events;
    }

    public ScheduleChangeView propose(
            String sessionId,
            boolean cancel,
            String room,
            Instant start,
            Instant end,
            String reason) {
        var s =
                sessions.findById(sessionId)
                        .orElseThrow(() -> BusinessException.missing("Timetable session"));
        try {
            access.requireLecturer(s.getTeachingClassId());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            access.requireStaff("ACADEMIC_STAFF", s.getTeachingClassId());
        }
        if (!s.getStatus().equals("PLANNED")
                || s.getStartsAt() == null
                || !Instant.now().isBefore(s.getStartsAt()))
            throw BusinessException.conflict(
                    "Only future published sessions can be changed or cancelled");
        if (!cancel
                && (room == null
                        || start == null
                        || end == null
                        || !end.isAfter(start)
                        || !start.isAfter(Instant.now())))
            throw new IllegalArgumentException("Supply a future replacement room/time interval");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO"
                    + " schedule_change(id,timetable_session_id,cancel,room_id,starts_at,ends_at,reason,proposed_by,expected_version,status)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,'PENDING')",
                id,
                sessionId,
                cancel,
                room,
                start == null ? null : Timestamp.from(start),
                end == null ? null : Timestamp.from(end),
                reason,
                Access.username(),
                s.getVersion());
        audit.record("timetable", id, "PROPOSE_CHANGE", reason);
        return row(id);
    }

    public ScheduleChangeView decide(String id, boolean approve, String reason) {
        row(id);
        commands.scheduleLock();
        jdbc.queryForObject(
                "SELECT id FROM schedule_change WHERE id=? FOR UPDATE", String.class, id);
        var p = row(id);
        var s = sessions.lockById(p.timetableSessionId()).orElseThrow();
        access.requireStaff("ACADEMIC_APPROVER", s.getTeachingClassId());
        if (Access.username().equals(p.proposedBy()))
            throw BusinessException.conflict("Proposer cannot approve own schedule change");
        if (!p.status().equals("PENDING"))
            throw BusinessException.conflict("Change already decided");
        if (approve) {
            if (!s.getStatus().equals("PLANNED")
                    || s.getVersion() != p.expectedVersion()
                    || !Instant.now().isBefore(s.getStartsAt()))
                throw BusinessException.conflict("Published schedule changed or already started");
            if (!p.cancel()) {
                if (!p.startsAt().isAfter(Instant.now()))
                    throw BusinessException.conflict("Replacement start time has passed");
                var c = classes.findClassById(s.getTeachingClassId()).orElseThrow();
                var room =
                        organization
                                .findRoomById(p.roomId())
                                .orElseThrow(() -> BusinessException.missing("Room"));
                if (room.capacity() < c.maxCapacity())
                    throw BusinessException.conflict("Room smaller than class capacity");
                commands.checkConflict(
                        p.roomId(), c.lecturerId(), c.id(), p.startsAt(), p.endsAt(), s.getId());
            }
            jdbc.update(
                    "INSERT INTO"
                        + " timetable_history(id,timetable_session_id,version,room_id,starts_at,ends_at,status,change_id)"
                        + " VALUES (?,?,?,?,?,?,?,?)",
                    UUID.randomUUID().toString(),
                    s.getId(),
                    s.getVersion(),
                    s.getRoomId(),
                    Timestamp.from(s.getStartsAt()),
                    Timestamp.from(s.getEndsAt()),
                    s.getStatus(),
                    id);
            if (p.cancel()) s.setStatus("CANCELLED");
            else s.move(p.roomId(), p.startsAt(), p.endsAt());
            sessions.flush();
            events.publishEvent(new ScheduleChanged(s.getId(), s.getTeachingClassId(), p.reason()));
        }
        jdbc.update(
                "UPDATE schedule_change SET"
                    + " status=?,decided_by=?,decision_reason=?,decided_at=CURRENT_TIMESTAMP WHERE"
                    + " id=?",
                approve ? "APPROVED" : "REJECTED",
                Access.username(),
                reason,
                id);
        audit.record("timetable", id, "DECIDE_CHANGE", reason);
        return row(id);
    }

    @Transactional(readOnly = true)
    public ScheduleChangeView read(String id) {
        var p = row(id);
        var s = timetable.findSessionById(p.timetableSessionId()).orElseThrow();
        if (!access.canRead(s.teachingClassId()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Change outside assigned scope");
        return p;
    }

    private ScheduleChangeView row(String id) {
        var list =
                jdbc.query(
                        "SELECT * FROM schedule_change WHERE id=?",
                        (rs, n) ->
                                new ScheduleChangeView(
                                        rs.getString("id"),
                                        rs.getString("timetable_session_id"),
                                        rs.getBoolean("cancel"),
                                        rs.getString("room_id"),
                                        rs.getTimestamp("starts_at") == null
                                                ? null
                                                : rs.getTimestamp("starts_at").toInstant(),
                                        rs.getTimestamp("ends_at") == null
                                                ? null
                                                : rs.getTimestamp("ends_at").toInstant(),
                                        rs.getString("reason"),
                                        rs.getString("proposed_by"),
                                        rs.getLong("expected_version"),
                                        rs.getString("status")),
                        id);
        if (list.isEmpty()) throw BusinessException.missing("Schedule change");
        return list.getFirst();
    }
}
