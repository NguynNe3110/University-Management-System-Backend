package com.university.academic.internal.application;

import com.university.academic.api.*;
import com.university.academic.internal.domain.Program;
import com.university.academic.internal.persistence.ProgramRepository;
import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProgramCommands {
    private final ProgramRepository repository;
    private final TechnicalAudit audit;
    private final OrganizationDirectory organization;

    public ProgramCommands(
            ProgramRepository repository,
            TechnicalAudit audit,
            OrganizationDirectory organization) {
        this.repository = repository;
        this.audit = audit;
        this.organization = organization;
    }

    public ProgramView create(ProgramRequest r) {
        if (organization.findDepartmentById(r.departmentId()).isEmpty())
            throw BusinessException.missing("Department");
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", r.departmentId());
        var e = new Program(UUID.randomUUID().toString(), r.code(), r.name(), r.departmentId());
        repository.saveAndFlush(e);
        audit.record("academic", e.getId(), "CREATE_PROGRAM", r.toString());
        return view(e);
    }

    public ProgramView update(String id, ProgramRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Program"));
        if (organization.findDepartmentById(r.departmentId()).isEmpty())
            throw BusinessException.missing("Department");
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", e.getDepartmentId());
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", r.departmentId());
        e.revise(r.code(), r.name(), r.departmentId(), r.version());
        repository.flush();
        audit.record("academic", id, "UPDATE_PROGRAM", r.toString());
        return view(e);
    }

    private ProgramView view(Program e) {
        return new ProgramView(
                e.getId(), e.getCode(), e.getName(), e.getDepartmentId(), e.getVersion());
    }
}
