package com.university.grade.internal.application;

import com.university.enrollment.api.*;
import com.university.grade.api.*;
import com.university.grade.internal.domain.*;
import com.university.grade.internal.persistence.*;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GradeCommands implements GradeEligibility {
    private final GradeBatchRepository batches;
    private final StudentGradeRepository grades;
    private final EnrollmentRegistry enrollment;
    private final TeachingClassDirectory classes;
    private final ClassAccess access;
    private final TechnicalAudit audit;
    private final JdbcTemplate jdbc;
    private final org.springframework.context.ApplicationEventPublisher events;

    public GradeCommands(
            GradeBatchRepository batches,
            StudentGradeRepository grades,
            EnrollmentRegistry enrollment,
            TeachingClassDirectory classes,
            ClassAccess access,
            TechnicalAudit audit,
            JdbcTemplate jdbc,
            org.springframework.context.ApplicationEventPublisher events) {
        this.batches = batches;
        this.grades = grades;
        this.enrollment = enrollment;
        this.classes = classes;
        this.access = access;
        this.audit = audit;
        this.jdbc = jdbc;
        this.events = events;
    }

    public GradeBatchView save(String classId, GradeBatchRequest r) {
        access.requireLecturer(classId);
        classLock(classId);
        var batch = batches.findById(classId).orElseGet(() -> new GradeBatch(classId));
        if (!batch.getStatus().equals("DRAFT") || batch.getVersion() != r.version())
            throw BusinessException.conflict("Expected current DRAFT grade batch");
        if (!Double.isFinite(r.attendanceWeight() + r.midtermWeight() + r.finalWeight())
                || Math.abs(r.attendanceWeight() + r.midtermWeight() + r.finalWeight() - 1)
                        > 0.000001)
            throw new IllegalArgumentException("Grade weights must sum to 1");
        var ids = new HashSet<String>();
        for (var e : r.grades()) {
            if (!Double.isFinite(e.attendanceScore() + e.midtermScore() + e.finalScore()))
                throw new IllegalArgumentException("Invalid score");
            if (!ids.add(e.studentId())) throw new IllegalArgumentException("Duplicate student");
            if (!enrollment.isEnrolled(e.studentId(), classId))
                throw BusinessException.conflict("Student not enrolled in this class");
        }
        batch.configure(r);
        batches.saveAndFlush(batch);
        grades.deleteAll(grades.findByTeachingClassId(classId));
        grades.flush();
        for (var e : r.grades())
            grades.save(
                    new StudentGrade(
                            UUID.randomUUID().toString(),
                            e.studentId(),
                            classId,
                            e.attendanceScore(),
                            e.midtermScore(),
                            e.finalScore(),
                            total(e, batch),
                            "DRAFT"));
        grades.flush();
        audit.record("grade", classId, "SAVE_DRAFT", r.toString());
        return view(batch);
    }

    public GradeBatchView submit(String classId, long version) {
        access.requireLecturer(classId);
        classLock(classId);
        var b = locked(classId, version, "DRAFT");
        var actual =
                new HashSet<>(
                        grades.findByTeachingClassId(classId).stream()
                                .map(StudentGrade::getStudentId)
                                .toList());
        var roster = new HashSet<>(enrollment.activeStudentIds(classId));
        if (roster.isEmpty() || !roster.equals(actual))
            throw BusinessException.conflict(
                    "A complete active class roster must have grades before submission");
        b.submit(Access.username());
        for (var g : grades.findByTeachingClassId(classId)) g.setStatus("SUBMITTED");
        batches.flush();
        audit.record("grade", classId, "SUBMIT", "Full class submitted");
        return view(b);
    }

    public GradeBatchView publish(String classId, long version) {
        access.requireStaff("EXAM_STAFF", classId);
        classLock(classId);
        var b = locked(classId, version, "SUBMITTED");
        if (Access.username().equals(b.getSubmittedBy()))
            throw BusinessException.conflict("Submitter cannot publish own grade batch");
        b.publish(Access.username());
        for (var g : grades.findByTeachingClassId(classId)) g.setStatus("PUBLISHED");
        batches.flush();
        audit.record("grade", classId, "PUBLISH", "Full class published");
        events.publishEvent(
                new GradesPublished(
                        classId,
                        grades.findByTeachingClassId(classId).stream()
                                .map(StudentGrade::getStudentId)
                                .toList()));
        return view(b);
    }

    @Transactional(readOnly = true)
    public GradeBatchView read(String classId) {
        if (!access.canRead(classId))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Class outside assigned scope");
        return view(
                batches.findById(classId)
                        .orElseThrow(() -> BusinessException.missing("Grade batch")));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassedCourse(String student, String course, double minimum) {
        return grades.findByStudentId(student).stream()
                .filter(
                        g ->
                                g.getStatus().equals("PUBLISHED")
                                        && g.getTotalScore() != null
                                        && g.getTotalScore() >= minimum)
                .anyMatch(
                        g ->
                                classes.findClassById(g.getTeachingClassId())
                                        .map(c -> c.courseId().equals(course))
                                        .orElse(false));
    }

    private void classLock(String id) {
        classes.lockForRegistration(id);
    }

    private GradeBatch locked(String id, long version, String status) {
        var b = batches.lockById(id).orElseThrow(() -> BusinessException.missing("Grade batch"));
        if (b.getVersion() != version || !b.getStatus().equals(status))
            throw BusinessException.conflict("Grade batch changed or invalid state");
        return b;
    }

    private double total(GradeBatchRequest.Entry e, GradeBatch b) {
        return Math.round(
                        (e.attendanceScore() * b.getAttendanceWeight()
                                        + e.midtermScore() * b.getMidtermWeight()
                                        + e.finalScore() * b.getFinalWeight())
                                * 100.0)
                / 100.0;
    }

    private GradeBatchView view(GradeBatch b) {
        return new GradeBatchView(
                b.getClassId(),
                b.getStatus(),
                b.getVersion(),
                b.getAttendanceWeight(),
                b.getMidtermWeight(),
                b.getFinalWeight(),
                grades.findByTeachingClassId(b.getClassId()).stream()
                        .map(
                                g ->
                                        new StudentGradeView(
                                                g.getId(),
                                                g.getStudentId(),
                                                g.getTeachingClassId(),
                                                g.getAttendanceScore(),
                                                g.getMidtermScore(),
                                                g.getFinalScore(),
                                                g.getTotalScore(),
                                                g.getStatus(),
                                                g.getResultRevision()))
                        .toList());
    }
}
