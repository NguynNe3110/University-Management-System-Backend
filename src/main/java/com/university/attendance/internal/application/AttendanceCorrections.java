package com.university.attendance.internal.application;

import com.university.attendance.api.AttendanceCorrectionView;
import com.university.attendance.internal.persistence.*;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.ClassAccess;
import com.university.timetable.api.TimetableCatalog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AttendanceCorrections {
    private final AttendanceSessionRepository sessions;
    private final AttendanceRecordRepository records;
    private final TimetableCatalog timetable;
    private final ClassAccess access;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;

    public AttendanceCorrections(
            AttendanceSessionRepository sessions,
            AttendanceRecordRepository records,
            TimetableCatalog timetable,
            ClassAccess access,
            JdbcTemplate jdbc,
            TechnicalAudit audit) {
        this.sessions = sessions;
        this.records = records;
        this.timetable = timetable;
        this.access = access;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    public AttendanceCorrectionView propose(
            String sessionId, String student, String status, String reason) {
        var s =
                sessions.lockById(sessionId)
                        .orElseThrow(() -> BusinessException.missing("Attendance session"));
        if (!Access.self("STUDENT", student))
            access.requireLecturer(
                    timetable.findSessionById(s.getSessionId()).orElseThrow().teachingClassId());
        if (!s.getStatus().equals("CLOSED") || !s.getStudentIds().contains(student))
            throw BusinessException.conflict(
                    "Correction requires a closed roster containing the student");
        var r = records.findByAttendanceSessionIdAndStudentId(sessionId, student).orElseThrow();
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO"
                    + " attendance_correction(id,attendance_session_id,student_id,requested_status,original_status,reason,proposed_by,status)"
                    + " VALUES (?,?,?,?,?,?,?,'PENDING')",
                id,
                sessionId,
                student,
                status,
                r.getStatus(),
                reason,
                Access.username());
        audit.record("attendance", id, "PROPOSE_CORRECTION", reason);
        return row(id);
    }

    public AttendanceCorrectionView decide(String id, boolean approve, String reason) {
        var p = row(id);
        var s = sessions.lockById(p.attendanceSessionId()).orElseThrow();
        jdbc.queryForObject(
                "SELECT id FROM attendance_correction WHERE id=? FOR UPDATE", String.class, id);
        p = row(id);
        access.requireStaff(
                "FACULTY_STAFF",
                timetable.findSessionById(s.getSessionId()).orElseThrow().teachingClassId());
        if (Access.username().equals(p.proposedBy()))
            throw BusinessException.conflict("Proposer cannot approve own correction");
        if (!p.status().equals("PENDING"))
            throw BusinessException.conflict("Correction already decided");
        var r =
                records.findByAttendanceSessionIdAndStudentId(
                                p.attendanceSessionId(), p.studentId())
                        .orElseThrow();
        if (approve) {
            String original =
                    jdbc.queryForObject(
                            "SELECT original_status FROM attendance_correction WHERE id=?",
                            String.class,
                            id);
            if (!r.getStatus().equals(original))
                throw BusinessException.conflict("Attendance changed; submit a new proposal");
            jdbc.update(
                    "INSERT INTO"
                        + " attendance_result_history(id,attendance_record_id,previous_status,correction_id)"
                        + " VALUES (?,?,?,?)",
                    UUID.randomUUID().toString(),
                    r.getId(),
                    r.getStatus(),
                    id);
            r.correctedStatus(p.requestedStatus());
            records.flush();
        }
        jdbc.update(
                "UPDATE attendance_correction SET"
                    + " status=?,decided_by=?,decision_reason=?,decided_at=CURRENT_TIMESTAMP WHERE"
                    + " id=?",
                approve ? "APPROVED" : "REJECTED",
                Access.username(),
                reason,
                id);
        audit.record("attendance", id, "DECIDE_CORRECTION", reason);
        return row(id);
    }

    private AttendanceCorrectionView row(String id) {
        var list =
                jdbc.query(
                        "SELECT * FROM attendance_correction WHERE id=?",
                        (rs, n) ->
                                new AttendanceCorrectionView(
                                        rs.getString("id"),
                                        rs.getString("attendance_session_id"),
                                        rs.getString("student_id"),
                                        rs.getString("requested_status"),
                                        rs.getString("reason"),
                                        rs.getString("proposed_by"),
                                        rs.getString("status")),
                        id);
        if (list.isEmpty()) throw BusinessException.missing("Attendance correction");
        return list.getFirst();
    }
}
