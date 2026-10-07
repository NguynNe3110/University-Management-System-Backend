package com.university.enrollment.internal.application;

import com.university.academic.api.AcademicCatalog;
import com.university.course.api.CourseCatalog;
import com.university.enrollment.api.*;
import com.university.enrollment.internal.domain.*;
import com.university.enrollment.internal.persistence.*;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.student.api.StudentDirectory;
import com.university.teachingclass.api.*;
import com.university.timetable.api.TimetableCatalog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class EnrollmentRegistryImpl implements EnrollmentRegistry {
    private final StudentEnrollmentRepository repository;
    private final RegistrationWindowRepository windows;
    private final TeachingClassDirectory classes;
    private final StudentDirectory students;
    private final AcademicCatalog academic;
    private final com.university.academic.api.AcademicStructure structure;
    private final CourseCatalog courses;
    private final TimetableCatalog timetable;
    private final GradeEligibility eligibility;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

    public EnrollmentRegistryImpl(
            StudentEnrollmentRepository repository,
            RegistrationWindowRepository windows,
            TeachingClassDirectory classes,
            StudentDirectory students,
            AcademicCatalog academic,
            com.university.academic.api.AcademicStructure structure,
            CourseCatalog courses,
            TimetableCatalog timetable,
            @org.springframework.context.annotation.Lazy GradeEligibility eligibility,
            JdbcTemplate jdbc,
            TechnicalAudit audit) {
        this.repository = repository;
        this.windows = windows;
        this.classes = classes;
        this.students = students;
        this.academic = academic;
        this.structure = structure;
        this.courses = courses;
        this.timetable = timetable;
        this.eligibility = eligibility;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentView> findById(String id) {
        return repository.findById(id).map(this::view);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentView> findByStudentId(String id) {
        return repository.findByStudentId(id).stream().map(this::view).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentView> findByTeachingClassId(String id) {
        return repository.findByTeachingClassId(id).stream().map(this::view).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(String studentId, String classId) {
        return repository
                .findByStudentIdAndTeachingClassId(studentId, classId)
                .map(e -> "ENROLLED".equals(e.getStatus()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> activeStudentIds(String classId) {
        return repository.findByTeachingClassId(classId).stream()
                .filter(e -> "ENROLLED".equals(e.getStatus()))
                .map(StudentEnrollment::getStudentId)
                .toList();
    }

    public WindowView createWindow(WindowRequest r) {
        var program =
                academic.findProgramById(r.programId())
                        .orElseThrow(() -> BusinessException.missing("Program"));
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", program.departmentId());
        if (academic.findSemesterById(r.semesterId()).isEmpty())
            throw BusinessException.missing("Semester");
        if (!r.closesAt().isAfter(r.opensAt()) || r.cancellationDeadline().isBefore(r.opensAt()))
            throw new IllegalArgumentException("Invalid registration window interval");
        if (!Double.isFinite(r.minimumPassingScore()))
            throw new IllegalArgumentException("Invalid passing score");
        for (var id : r.prerequisiteCourseIds())
            if (courses.findById(UUID.fromString(id)).isEmpty())
                throw BusinessException.missing("Prerequisite course");
        var w = windows.saveAndFlush(new RegistrationWindow(r));
        audit.record("enrollment", w.view().id(), "CREATE_WINDOW", r.toString());
        return w.view();
    }

    @Transactional(readOnly = true)
    public List<WindowView> listWindows() {
        return windows.findAll().stream().map(RegistrationWindow::view).toList();
    }

    @Override
    public EnrollmentView enroll(String studentId, String classId) {
        throw new IllegalArgumentException("windowId is required");
    }

    @Override
    public EnrollmentView enroll(String studentId, String classId, String windowId) {
        Access.requireSelf("STUDENT", studentId);
        if (!Access.authority("ROLE_STUDENT"))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Student role required");
        studentLock(studentId);
        var c = classes.lockForRegistration(classId);
        var existing = repository.findByStudentIdAndTeachingClassId(studentId, classId);
        if (existing.isPresent() && existing.get().getStatus().equals("ENROLLED"))
            return view(existing.get());
        validate(studentId, c, windowId, null);
        var e =
                existing.orElseGet(
                        () ->
                                new StudentEnrollment(
                                        UUID.randomUUID().toString(),
                                        studentId,
                                        classId,
                                        "ENROLLED"));
        e.activate(windowId);
        repository.saveAndFlush(e);
        audit.record("enrollment", e.getId(), "ENROLL", classId);
        return view(e);
    }

    @Override
    public EnrollmentView cancel(String id) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Enrollment"));
        Access.requireSelf("STUDENT", e.getStudentId());
        if (!Access.authority("ROLE_STUDENT"))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Student role required");
        studentLock(e.getStudentId());
        classes.lockForRegistration(e.getTeachingClassId());
        entityManager.refresh(e);
        if (e.getStatus().equals("CANCELLED")) return view(e);
        cancellation(e);
        e.cancel();
        repository.flush();
        audit.record("enrollment", id, "CANCEL", "Cancelled by student");
        return view(e);
    }

    @Override
    public EnrollmentView transfer(String id, String target, String windowId) {
        var old =
                repository.findById(id).orElseThrow(() -> BusinessException.missing("Enrollment"));
        Access.requireSelf("STUDENT", old.getStudentId());
        if (!Access.authority("ROLE_STUDENT"))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Student role required");
        if (old.getTeachingClassId().equals(target))
            throw new IllegalArgumentException("Choose a different class");
        studentLock(old.getStudentId());
        // Stable lock order prevents reverse transfers from deadlocking.
        var ids = new ArrayList<>(List.of(old.getTeachingClassId(), target));
        Collections.sort(ids);
        for (var classId : ids) classes.lockForRegistration(classId);
        entityManager.refresh(old);
        if (!old.getStatus().equals("ENROLLED"))
            throw BusinessException.conflict("Original enrollment is not active");
        cancellation(old);
        var source = classes.findClassById(old.getTeachingClassId()).orElseThrow();
        var destination = classes.findClassById(target).orElseThrow();
        if (!source.courseId().equals(destination.courseId())
                || !source.semesterId().equals(destination.semesterId())
                || source.tuitionRate() == null
                || destination.tuitionRate() == null
                || source.tuitionRate().compareTo(destination.tuitionRate()) != 0)
            throw BusinessException.conflict(
                    "Transfer only supports the same course, semester and tuition rate");
        if (isEnrolled(old.getStudentId(), target))
            throw BusinessException.conflict("Already enrolled in target class");
        validate(old.getStudentId(), destination, windowId, old.getTeachingClassId());
        var next =
                repository
                        .findByStudentIdAndTeachingClassId(old.getStudentId(), target)
                        .orElseGet(
                                () ->
                                        new StudentEnrollment(
                                                UUID.randomUUID().toString(),
                                                old.getStudentId(),
                                                target,
                                                "ENROLLED"));
        old.cancel();
        next.activate(windowId);
        repository.saveAndFlush(next);
        audit.record("enrollment", id, "TRANSFER", next.getId() + " target=" + target);
        return view(next);
    }

    private void studentLock(String id) {
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "student:" + id);
    }

    private void cancellation(StudentEnrollment e) {
        if (e.getWindowId() == null)
            throw BusinessException.conflict("Legacy enrollment has no cancellation policy");
        var w = windows.findById(e.getWindowId()).orElseThrow().view();
        if (Instant.now().isAfter(w.cancellationDeadline()))
            throw BusinessException.conflict("Cancellation deadline passed");
    }

    private void validate(
            String studentId, TeachingClassView c, String windowId, String ignoredClass) {
        var s =
                students.findById(studentId)
                        .orElseThrow(() -> BusinessException.missing("Student"));
        var w =
                windows.findById(windowId)
                        .orElseThrow(() -> BusinessException.missing("Registration window"))
                        .view();
        var now = Instant.now();
        if (!s.status().equals("ACTIVE") || !c.status().equals("OPEN"))
            throw BusinessException.conflict("Student inactive or class not open");
        if (!w.programId().equals(s.programId()) || !w.semesterId().equals(c.semesterId()))
            throw BusinessException.conflict(
                    "Window does not match student program and class semester");
        if (now.isBefore(w.opensAt()) || !now.isBefore(w.closesAt()))
            throw BusinessException.conflict("Registration window closed");
        if (c.maxCapacity() < 1
                || repository.countByTeachingClassIdAndStatus(c.id(), "ENROLLED")
                        >= c.maxCapacity()) throw BusinessException.conflict("Class full");
        for (var course : w.prerequisiteCourseIds())
            if (!eligibility.hasPassedCourse(studentId, course, w.minimumPassingScore()))
                throw BusinessException.conflict(
                        "Published prerequisite result is missing or insufficient");
        var curriculum = structure.curriculum(s.programId());
        if (curriculum.courses().isEmpty())
            throw BusinessException.conflict("Student program curriculum is not configured");
        {
            var entry =
                    curriculum.courses().stream()
                            .filter(row -> row.courseId().equals(c.courseId()))
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            BusinessException.conflict(
                                                    "Course outside student curriculum"));
            for (var required : entry.prerequisiteCourseIds())
                if (!eligibility.hasPassedCourse(studentId, required, w.minimumPassingScore()))
                    throw BusinessException.conflict(
                            "Published curriculum prerequisite result is missing or insufficient");
        }
        var wanted = courses.findById(UUID.fromString(c.courseId())).orElseThrow();
        if (!wanted.active()) throw BusinessException.conflict("Course archived");
        int credits = wanted.credits();
        for (var e : repository.findByStudentId(studentId)) {
            if (!e.getStatus().equals("ENROLLED") || e.getTeachingClassId().equals(ignoredClass))
                continue;
            var other = classes.findClassById(e.getTeachingClassId()).orElseThrow();
            if (!other.semesterId().equals(c.semesterId())) continue;
            if (other.courseId().equals(c.courseId()))
                throw BusinessException.conflict(
                        "Already registered for this course in this semester");
            credits += courses.findById(UUID.fromString(other.courseId())).orElseThrow().credits();
            for (var a : timetable.findSessionsByTeachingClassId(c.id()))
                for (var b : timetable.findSessionsByTeachingClassId(other.id())) {
                    if (a.status().equals("PLANNED")
                            && b.status().equals("PLANNED")
                            && a.startsAt() != null
                            && b.startsAt() != null
                            && a.startsAt().isBefore(b.endsAt())
                            && a.endsAt().isAfter(b.startsAt()))
                        throw BusinessException.conflict("Student timetable conflict");
                }
        }
        if (credits > w.maxCredits()) throw BusinessException.conflict("Credit limit exceeded");
    }

    private EnrollmentView view(StudentEnrollment e) {
        return new EnrollmentView(
                e.getId(),
                e.getStudentId(),
                e.getTeachingClassId(),
                e.getStatus(),
                e.getEnrolledAt());
    }
}
