package com.university.academic.internal.application;

import com.university.academic.api.*;
import com.university.academic.internal.domain.Semester;
import com.university.academic.internal.persistence.SemesterRepository;
import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SemesterCommands {
    private final SemesterRepository repository;
    private final TechnicalAudit audit;
    private final OrganizationDirectory organization;

    public SemesterCommands(
            SemesterRepository repository,
            TechnicalAudit audit,
            OrganizationDirectory organization) {
        this.repository = repository;
        this.audit = audit;
        this.organization = organization;
    }

    public SemesterView create(SemesterRequest r) {

        Access.require("ACADEMIC_STAFF", "GLOBAL", "*");
        var e = new Semester(UUID.randomUUID().toString(), r.code(), r.academicYear(), r.term());
        repository.saveAndFlush(e);
        audit.record("academic", e.getId(), "CREATE_SEMESTER", r.toString());
        return view(e);
    }

    public SemesterView update(String id, SemesterRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Semester"));

        Access.require("ACADEMIC_STAFF", "GLOBAL", "*");
        e.revise(r.code(), r.academicYear(), r.term(), r.version());
        repository.flush();
        audit.record("academic", id, "UPDATE_SEMESTER", r.toString());
        return view(e);
    }

    private SemesterView view(Semester e) {
        return new SemesterView(
                e.getId(), e.getCode(), e.getAcademicYear(), e.getTerm(), e.getVersion());
    }
}
