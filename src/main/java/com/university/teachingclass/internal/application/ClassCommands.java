package com.university.teachingclass.internal.application;

import com.university.academic.api.AcademicCatalog;
import com.university.course.api.CourseCatalog;
import com.university.lecturer.api.LecturerDirectory;
import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.teachingclass.api.*;
import com.university.teachingclass.internal.domain.TeachingClass;
import com.university.teachingclass.internal.persistence.TeachingClassRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClassCommands {
    private final TeachingClassRepository repository;
    private final TeachingClassDirectory classes;
    private final ClassAccess access;
    private final AcademicCatalog academic;
    private final CourseCatalog courses;
    private final LecturerDirectory lecturers;
    private final OrganizationDirectory organization;
    private final TechnicalAudit audit;

    public ClassCommands(
            TeachingClassRepository repository,
            TeachingClassDirectory classes,
            ClassAccess access,
            AcademicCatalog academic,
            CourseCatalog courses,
            LecturerDirectory lecturers,
            OrganizationDirectory organization,
            TechnicalAudit audit) {
        this.repository = repository;
        this.classes = classes;
        this.access = access;
        this.academic = academic;
        this.courses = courses;
        this.lecturers = lecturers;
        this.organization = organization;
        this.audit = audit;
    }

    public TeachingClassView create(ClassRequest r) {
        if (!Access.can("ACADEMIC_STAFF", "DEPARTMENT", r.departmentId()))
            Access.require("FACULTY_STAFF", "DEPARTMENT", r.departmentId());
        var course =
                courses.findById(UUID.fromString(r.courseId()))
                        .orElseThrow(() -> BusinessException.missing("Course"));
        if (!course.active()) throw BusinessException.conflict("Course archived");
        if (academic.findSemesterById(r.semesterId()).isEmpty())
            throw BusinessException.missing("Semester");
        if (organization.findDepartmentById(r.departmentId()).isEmpty())
            throw BusinessException.missing("Department");
        var lecturer =
                lecturers
                        .findById(r.lecturerId())
                        .orElseThrow(() -> BusinessException.missing("Lecturer"));
        if (!lecturer.status().equals("ACTIVE"))
            throw BusinessException.conflict("Lecturer inactive");
        var c =
                new TeachingClass(
                        UUID.randomUUID().toString(),
                        r.code(),
                        r.courseId(),
                        r.semesterId(),
                        null,
                        r.lecturerId(),
                        r.maxCapacity(),
                        "PROPOSED");
        c.configure(r.departmentId(), r.tuitionRate());
        repository.saveAndFlush(c);
        audit.record("teachingclass", c.getId(), "PROPOSE", r.toString());
        return classes.findClassById(c.getId()).orElseThrow();
    }

    public TeachingClassView open(String id, long version) {
        access.requireStaff("ACADEMIC_STAFF", id);
        var c = repository.lockById(id).orElseThrow(() -> BusinessException.missing("Class"));
        if (c.getVersion() != version || !c.getStatus().equals("PROPOSED"))
            throw BusinessException.conflict("Expected current PROPOSED class");
        c.setStatus("OPEN");
        repository.flush();
        audit.record("teachingclass", id, "OPEN", "Class opened");
        return classes.findClassById(id).orElseThrow();
    }
}
