package com.university.student.internal.application;

import com.university.academic.api.AcademicCatalog;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.student.api.*;
import com.university.student.internal.domain.StudentProfile;
import com.university.student.internal.persistence.StudentProfileRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StudentCommands {
    private final StudentProfileRepository repository;
    private final TechnicalAudit audit;
    private final AcademicCatalog academic;
    private final com.university.academic.api.AcademicStructure structure;

    public StudentCommands(
            StudentProfileRepository repository,
            TechnicalAudit audit,
            AcademicCatalog academic,
            com.university.academic.api.AcademicStructure structure) {
        this.repository = repository;
        this.audit = audit;
        this.academic = academic;
        this.structure = structure;
    }

    public StudentProfileView create(StudentProfileRequest r) {
        var program =
                academic.findProgramById(r.programId())
                        .orElseThrow(() -> BusinessException.missing("Program"));
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", program.departmentId());
        var e =
                new StudentProfile(
                        UUID.randomUUID().toString(),
                        r.studentCode(),
                        r.fullName(),
                        r.email(),
                        r.programId(),
                        r.status());
        link(e, r);
        repository.saveAndFlush(e);
        audit.record("student", e.getId(), "CREATE_STUDENT", r.toString());
        return view(e);
    }

    public StudentProfileView update(String id, StudentProfileRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Student"));
        var program =
                academic.findProgramById(r.programId())
                        .orElseThrow(() -> BusinessException.missing("Program"));
        Access.require(
                "ACADEMIC_STAFF",
                "DEPARTMENT",
                academic.findProgramById(e.getProgramId()).orElseThrow().departmentId());
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", program.departmentId());
        e.revise(r.studentCode(), r.fullName(), r.email(), r.programId(), r.status(), r.version());
        link(e, r);
        repository.flush();
        audit.record("student", id, "UPDATE_STUDENT", r.toString());
        return view(e);
    }

    private void link(
            com.university.student.internal.domain.StudentProfile e, StudentProfileRequest r) {
        if (r.administrativeClassId() != null) {
            var c =
                    structure
                            .findAdministrativeClass(r.administrativeClassId())
                            .orElseThrow(() -> BusinessException.missing("Administrative class"));
            if (!c.programId().equals(r.programId()))
                throw new IllegalArgumentException(
                        "Administrative class belongs to another program");
        }
        e.administrativeClass(r.administrativeClassId());
    }

    private StudentProfileView view(StudentProfile e) {
        return new StudentProfileView(
                e.getId(),
                e.getStudentCode(),
                e.getFullName(),
                e.getEmail(),
                e.getProgramId(),
                e.getStatus(),
                e.getVersion(),
                e.getAdministrativeClassId());
    }
}
