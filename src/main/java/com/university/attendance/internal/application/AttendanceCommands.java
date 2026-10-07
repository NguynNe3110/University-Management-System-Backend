package com.university.attendance.internal.application;

import com.university.attendance.api.*;
import com.university.attendance.internal.domain.*;
import com.university.attendance.internal.persistence.*;
import com.university.enrollment.api.EnrollmentRegistry;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.ClassAccess;
import com.university.timetable.api.TimetableCatalog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AttendanceCommands {
    private final AttendanceSessionRepository sessions;
    private final AttendanceRecordRepository records;
    private final EnrollmentRegistry enrollment;
    private final TimetableCatalog timetable;
    private final ClassAccess access;
    private final TechnicalAudit audit;
    private final JdbcTemplate jdbc;

    public AttendanceCommands(
            AttendanceSessionRepository sessions,
            AttendanceRecordRepository records,
            EnrollmentRegistry enrollment,
            TimetableCatalog timetable,
            ClassAccess access,
            TechnicalAudit audit,
            JdbcTemplate jdbc) {
        this.sessions = sessions;
        this.records = records;
        this.enrollment = enrollment;
        this.timetable = timetable;
        this.access = access;
        this.audit = audit;
        this.jdbc = jdbc;
    }

    public AttendanceSessionView open(String timetableSessionId, Instant expiresAt) {
        var t =
                timetable
                        .findSessionById(timetableSessionId)
                        .orElseThrow(() -> BusinessException.missing("Timetable session"));
        access.requireLecturer(t.teachingClassId());
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "attendance:" + timetableSessionId);
        if (sessions.findBySessionId(timetableSessionId).isPresent())
            throw BusinessException.conflict("Attendance session already exists");
        var now = Instant.now();
        if (!t.status().equals("PLANNED")
                || t.startsAt() == null
                || now.isBefore(t.startsAt())
                || !now.isBefore(t.endsAt()))
            throw BusinessException.conflict("Timetable session is not in its teaching interval");
        if (!expiresAt.isAfter(now) || expiresAt.isAfter(t.endsAt()))
            throw new IllegalArgumentException(
                    "QR expiry must be in the remaining session interval");
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var s =
                sessions.saveAndFlush(
                        new AttendanceSession(
                                timetableSessionId,
                                token,
                                expiresAt,
                                enrollment.activeStudentIds(t.teachingClassId())));
        audit.record(
                "attendance",
                s.getId(),
                "OPEN",
                "Timetable=" + timetableSessionId + "; roster=" + s.getStudentIds().size());
        return view(s);
    }

    public AttendanceSessionView close(String id) {
        var s = locked(id);
        var t = timetable.findSessionById(s.getSessionId()).orElseThrow();
        access.requireLecturer(t.teachingClassId());
        if (s.getStatus().equals("CLOSED")) return view(s);
        for (var student : s.getStudentIds())
            if (records.findByAttendanceSessionIdAndStudentId(id, student).isEmpty())
                records.save(
                        new AttendanceRecord(
                                UUID.randomUUID().toString(),
                                id,
                                student,
                                Instant.now(),
                                "ABSENT"));
        records.flush();
        s.close();
        sessions.flush();
        audit.record("attendance", id, "CLOSE", "Roster finalized");
        return view(s);
    }

    public AttendanceRecordView checkIn(String id, String token) {
        if (!Access.authority("ROLE_STUDENT"))
            throw new AccessDeniedException("Student role required");
        String student = Access.selfId("STUDENT");
        var s = locked(id);
        if (!MessageDigest.isEqual(
                s.getQrToken().getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8)))
            throw new AccessDeniedException("Invalid QR token");
        if (!s.getStudentIds().contains(student))
            throw new AccessDeniedException("Student is outside this session roster");
        var existing = records.findByAttendanceSessionIdAndStudentId(id, student);
        if (existing.isPresent()) return recordView(existing.get());
        if (!s.getStatus().equals("OPEN") || !Instant.now().isBefore(s.getExpiresAt()))
            throw BusinessException.conflict("Attendance session closed or QR expired");
        var r =
                records.saveAndFlush(
                        new AttendanceRecord(
                                UUID.randomUUID().toString(),
                                id,
                                student,
                                Instant.now(),
                                "PRESENT"));
        audit.record("attendance", r.getId(), "CHECK_IN", "Session=" + id);
        return recordView(r);
    }

    @Transactional(readOnly = true)
    public AttendanceSessionView session(String id) {
        var s =
                sessions.findById(id)
                        .orElseThrow(() -> BusinessException.missing("Attendance session"));
        access.requireLecturer(
                timetable.findSessionById(s.getSessionId()).orElseThrow().teachingClassId());
        return view(s);
    }

    @Transactional(readOnly = true)
    public void requireRosterRead(String id) {
        var s =
                sessions.findById(id)
                        .orElseThrow(() -> BusinessException.missing("Attendance session"));
        if (!access.canRead(
                timetable.findSessionById(s.getSessionId()).orElseThrow().teachingClassId()))
            throw new AccessDeniedException("Not assigned to this class");
    }

    private AttendanceSession locked(String id) {
        return sessions.lockById(id)
                .orElseThrow(() -> BusinessException.missing("Attendance session"));
    }

    private AttendanceSessionView view(AttendanceSession s) {
        return new AttendanceSessionView(
                s.getId(),
                s.getSessionId(),
                s.getQrToken(),
                s.getExpiresAt(),
                s.getStatus(),
                s.getStudentIds().size());
    }

    private AttendanceRecordView recordView(AttendanceRecord r) {
        return new AttendanceRecordView(
                r.getId(),
                r.getAttendanceSessionId(),
                r.getStudentId(),
                r.getCheckInTime(),
                r.getStatus());
    }
}
