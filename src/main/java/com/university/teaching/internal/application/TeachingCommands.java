package com.university.teaching.internal.application;

import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teaching.api.*;
import com.university.teaching.internal.domain.TeachingLog;
import com.university.teaching.internal.persistence.TeachingLogRepository;
import com.university.teachingclass.api.ClassAccess;
import com.university.timetable.api.TimetableCatalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TeachingCommands {
    private final TeachingLogRepository repository;
    private final TimetableCatalog timetable;
    private final ClassAccess access;
    private final TechnicalAudit audit;

    public TeachingCommands(
            TeachingLogRepository repository,
            TimetableCatalog timetable,
            ClassAccess access,
            TechnicalAudit audit) {
        this.repository = repository;
        this.timetable = timetable;
        this.access = access;
        this.audit = audit;
    }

    public TeachingLogView submit(String sessionId, int hours, String content) {
        var t =
                timetable
                        .findSessionById(sessionId)
                        .orElseThrow(() -> BusinessException.missing("Timetable session"));
        access.requireLecturer(t.teachingClassId());
        if (!t.status().equals("HELD"))
            throw BusinessException.conflict("Confirm HELD before submitting workload");
        if (hours < 1 || hours > t.endPeriod() - t.startPeriod() + 1)
            throw new IllegalArgumentException("Actual periods exceed scheduled periods");
        if (repository.findByTimetableSessionId(sessionId).isPresent())
            throw BusinessException.conflict("Teaching log already exists");
        var l =
                new TeachingLog(
                        UUID.randomUUID().toString(),
                        sessionId,
                        Access.selfId("LECTURER"),
                        hours,
                        content.trim(),
                        "SUBMITTED");
        l.submitted(Access.username());
        repository.saveAndFlush(l);
        audit.record("teaching", l.getId(), "SUBMIT", content);
        return view(l);
    }

    public TeachingLogView decide(String id, boolean approve, String reason) {
        var l =
                repository
                        .lockById(id)
                        .orElseThrow(() -> BusinessException.missing("Teaching log"));
        access.requireStaff(
                "FACULTY_STAFF",
                timetable
                        .findSessionById(l.getTimetableSessionId())
                        .orElseThrow()
                        .teachingClassId());
        if (Access.username().equals(l.getSubmittedBy())
                || Access.self("LECTURER", l.getLecturerId()))
            throw BusinessException.conflict("Cannot confirm own teaching log");
        if (!l.getStatus().equals("SUBMITTED"))
            throw BusinessException.conflict("Log is no longer awaiting a decision");
        l.decide(approve ? "APPROVED" : "REJECTED", Access.username(), reason);
        audit.record("teaching", id, "DECIDE", reason);
        return view(l);
    }

    private TeachingLogView view(TeachingLog l) {
        return new TeachingLogView(
                l.getId(),
                l.getTimetableSessionId(),
                l.getLecturerId(),
                l.getActualHours(),
                l.getContentSummary(),
                l.getStatus());
    }
}
