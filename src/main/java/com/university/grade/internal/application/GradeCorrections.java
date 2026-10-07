package com.university.grade.internal.application;

import com.university.grade.api.*;
import com.university.grade.internal.persistence.*;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.*;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GradeCorrections {
    private final StudentGradeRepository grades;
    private final GradeBatchRepository batches;
    private final ClassAccess access;
    private final TeachingClassDirectory classes;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;
    private final ApplicationEventPublisher events;

    public GradeCorrections(
            StudentGradeRepository grades,
            GradeBatchRepository batches,
            ClassAccess access,
            TeachingClassDirectory classes,
            JdbcTemplate jdbc,
            TechnicalAudit audit,
            ApplicationEventPublisher events) {
        this.grades = grades;
        this.batches = batches;
        this.access = access;
        this.classes = classes;
        this.jdbc = jdbc;
        this.audit = audit;
        this.events = events;
    }

    public GradeCorrectionView propose(
            String classId, GradeBatchRequest.Entry scores, String reason) {
        access.requireLecturer(classId);
        classes.lockForRegistration(classId);
        var g =
                grades.findByStudentIdAndTeachingClassId(scores.studentId(), classId)
                        .orElseThrow(() -> BusinessException.missing("Grade"));
        if (!g.getStatus().equals("PUBLISHED"))
            throw BusinessException.conflict("Only published results use corrections");
        if (!Double.isFinite(
                scores.attendanceScore() + scores.midtermScore() + scores.finalScore()))
            throw new IllegalArgumentException("Invalid score");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO"
                    + " grade_correction(id,teaching_class_id,student_id,attendance_score,midterm_score,final_score,reason,proposed_by,expected_revision,status)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,'PENDING')",
                id,
                classId,
                scores.studentId(),
                scores.attendanceScore(),
                scores.midtermScore(),
                scores.finalScore(),
                reason,
                Access.username(),
                g.getResultRevision());
        audit.record("grade", id, "PROPOSE_CORRECTION", reason);
        return readRow(id);
    }

    public GradeCorrectionView decide(String id, boolean approve, String reason) {
        var initial = readRow(id);
        classes.lockForRegistration(initial.teachingClassId());
        jdbc.queryForObject(
                "SELECT id FROM grade_correction WHERE id=? FOR UPDATE", String.class, id);
        var p = readRow(id);
        access.requireStaff("EXAM_STAFF", p.teachingClassId());
        if (Access.username().equals(p.proposedBy()))
            throw BusinessException.conflict("Proposer cannot approve own correction");
        if (!p.status().equals("PENDING"))
            throw BusinessException.conflict("Correction already decided");
        if (approve) {
            var g =
                    grades.findByStudentIdAndTeachingClassId(p.studentId(), p.teachingClassId())
                            .orElseThrow();
            if (g.getResultRevision() != p.expectedRevision())
                throw BusinessException.conflict("Published grade changed; submit a new proposal");
            jdbc.update(
                    "INSERT INTO"
                        + " grade_result_history(id,grade_id,result_revision,attendance_score,midterm_score,final_score,total_score,correction_id)"
                        + " VALUES (?,?,?,?,?,?,?,?)",
                    UUID.randomUUID().toString(),
                    g.getId(),
                    g.getResultRevision(),
                    g.getAttendanceScore(),
                    g.getMidtermScore(),
                    g.getFinalScore(),
                    g.getTotalScore(),
                    id);
            var b = batches.findById(p.teachingClassId()).orElseThrow();
            double total =
                    Math.round(
                                    (p.attendanceScore() * b.getAttendanceWeight()
                                                    + p.midtermScore() * b.getMidtermWeight()
                                                    + p.finalScore() * b.getFinalWeight())
                                            * 100.0)
                            / 100.0;
            g.correction(p.attendanceScore(), p.midtermScore(), p.finalScore(), total);
            grades.flush();
            events.publishEvent(new GradesPublished(p.teachingClassId(), List.of(p.studentId())));
        }
        jdbc.update(
                "UPDATE grade_correction SET"
                    + " status=?,decided_by=?,decision_reason=?,decided_at=CURRENT_TIMESTAMP WHERE"
                    + " id=?",
                approve ? "APPROVED" : "REJECTED",
                Access.username(),
                reason,
                id);
        audit.record("grade", id, "DECIDE_CORRECTION", reason);
        return readRow(id);
    }

    @Transactional(readOnly = true)
    public GradeCorrectionView read(String id) {
        var p = readRow(id);
        if (!access.canRead(p.teachingClassId()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Correction outside assigned scope");
        return p;
    }

    private GradeCorrectionView readRow(String id) {
        var list =
                jdbc.query(
                        "SELECT * FROM grade_correction WHERE id=?",
                        (rs, n) ->
                                new GradeCorrectionView(
                                        rs.getString("id"),
                                        rs.getString("teaching_class_id"),
                                        rs.getString("student_id"),
                                        rs.getDouble("attendance_score"),
                                        rs.getDouble("midterm_score"),
                                        rs.getDouble("final_score"),
                                        rs.getString("reason"),
                                        rs.getString("proposed_by"),
                                        rs.getLong("expected_revision"),
                                        rs.getString("status")),
                        id);
        if (list.isEmpty()) throw BusinessException.missing("Grade correction");
        return list.getFirst();
    }
}
