package com.university.timetable.internal.application;

import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.*;
import com.university.timetable.api.*;
import com.university.timetable.internal.domain.TimetableSession;
import com.university.timetable.internal.persistence.TimetableSessionRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.UUID;

@Service
@Transactional
public class SessionCommands {
    private final TimetableSessionRepository repository;
    private final TimetableCatalog timetable;
    private final TeachingClassDirectory classes;
    private final ClassAccess access;
    private final OrganizationDirectory organization;
    private final com.university.organization.api.RoomAvailability availability;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;

    public SessionCommands(
            TimetableSessionRepository repository,
            TimetableCatalog timetable,
            TeachingClassDirectory classes,
            ClassAccess access,
            OrganizationDirectory organization,
            com.university.organization.api.RoomAvailability availability,
            JdbcTemplate jdbc,
            TechnicalAudit audit) {
        this.repository = repository;
        this.timetable = timetable;
        this.classes = classes;
        this.access = access;
        this.organization = organization;
        this.availability = availability;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    public TimetableSessionView create(SessionRequest r) {
        access.requireStaff("ACADEMIC_STAFF", r.teachingClassId());
        if (r.endPeriod() < r.startPeriod() || !r.endsAt().isAfter(r.startsAt()))
            throw new IllegalArgumentException("Invalid session interval");
        if (!r.startsAt()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDate()
                .equals(r.sessionDate()))
            throw new IllegalArgumentException(
                    "Session date must match startsAt in Asia/Ho_Chi_Minh");
        var c =
                classes.findClassById(r.teachingClassId())
                        .orElseThrow(() -> BusinessException.missing("Class"));
        if (!c.status().equals("OPEN")) throw BusinessException.conflict("Class not open");
        var room =
                organization
                        .findRoomById(r.roomId())
                        .orElseThrow(() -> BusinessException.missing("Room"));
        if (room.capacity() < c.maxCapacity())
            throw BusinessException.conflict("Room smaller than class capacity");
        scheduleLock();
        checkConflict(
                r.roomId(), c.lecturerId(), r.teachingClassId(), r.startsAt(), r.endsAt(), null);
        var s =
                new TimetableSession(
                        UUID.randomUUID().toString(),
                        r.teachingClassId(),
                        r.sessionNumber(),
                        r.roomId(),
                        r.sessionDate(),
                        r.startPeriod(),
                        r.endPeriod(),
                        "DRAFT");
        s.timing(r.startsAt(), r.endsAt(), Access.username());
        repository.saveAndFlush(s);
        audit.record("timetable", s.getId(), "CREATE", r.toString());
        return timetable.findSessionById(s.getId()).orElseThrow();
    }

    public TimetableSessionView publish(String id, long version) {
        scheduleLock();
        var s = repository.lockById(id).orElseThrow(() -> BusinessException.missing("Session"));
        access.requireStaff("ACADEMIC_APPROVER", s.getTeachingClassId());
        if (Access.username().equals(s.getCreatedBy()))
            throw BusinessException.conflict("Creator cannot publish own schedule");
        if (s.getVersion() != version || !s.getStatus().equals("DRAFT"))
            throw BusinessException.conflict("Expected current DRAFT session");
        var c = classes.findClassById(s.getTeachingClassId()).orElseThrow();
        checkConflict(
                s.getRoomId(),
                c.lecturerId(),
                s.getTeachingClassId(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.getId());
        s.setStatus("PLANNED");
        repository.flush();
        audit.record("timetable", id, "PUBLISH", "Published schedule");
        return timetable.findSessionById(id).orElseThrow();
    }

    public TimetableSessionView held(String id, long version) {
        var s = repository.lockById(id).orElseThrow(() -> BusinessException.missing("Session"));
        access.requireLecturer(s.getTeachingClassId());
        if (s.getVersion() != version || !s.getStatus().equals("PLANNED"))
            throw BusinessException.conflict("Expected current PLANNED session");
        if (s.getEndsAt() == null || Instant.now().isBefore(s.getEndsAt()))
            throw BusinessException.conflict("Session has not ended");
        s.setStatus("HELD");
        repository.flush();
        audit.record("timetable", id, "HELD", "Confirmed after server end time");
        return timetable.findSessionById(id).orElseThrow();
    }

    void scheduleLock() {
        jdbc.queryForObject("SELECT id FROM scheduling_lock WHERE id=1 FOR UPDATE", Integer.class);
    }

    void checkConflict(
            String room,
            String lecturer,
            String classId,
            Instant start,
            Instant end,
            String ignore) {
        availability.requireAvailable(room, start, end);
        for (var s : repository.findAll()) {
            if (s.getId().equals(ignore)
                    || s.getStatus().equals("CANCELLED")
                    || s.getStartsAt() == null) continue;
            if (start.isBefore(s.getEndsAt()) && end.isAfter(s.getStartsAt())) {
                var other = classes.findClassById(s.getTeachingClassId()).orElseThrow();
                if (room.equals(s.getRoomId())
                        || classId.equals(s.getTeachingClassId())
                        || lecturer.equals(other.lecturerId()))
                    throw BusinessException.conflict("Room, lecturer or class timetable conflict");
            }
        }
    }
}
