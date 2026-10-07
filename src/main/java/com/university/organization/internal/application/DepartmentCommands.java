package com.university.organization.internal.application;

import com.university.organization.api.*;
import com.university.organization.internal.domain.Department;
import com.university.organization.internal.persistence.DepartmentRepository;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DepartmentCommands {
    private final DepartmentRepository repository;
    private final TechnicalAudit audit;

    public DepartmentCommands(DepartmentRepository repository, TechnicalAudit audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public DepartmentView create(DepartmentRequest r) {

        Access.require("ACADEMIC_STAFF", "GLOBAL", "*");
        var e = new Department(UUID.randomUUID().toString(), r.code(), r.name());
        repository.saveAndFlush(e);
        audit.record("organization", e.getId(), "CREATE_DEPARTMENT", r.toString());
        return view(e);
    }

    public DepartmentView update(String id, DepartmentRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Department"));

        Access.require("ACADEMIC_STAFF", "GLOBAL", "*");
        e.revise(r.code(), r.name(), r.version());
        repository.flush();
        audit.record("organization", id, "UPDATE_DEPARTMENT", r.toString());
        return view(e);
    }

    private DepartmentView view(Department e) {
        return new DepartmentView(e.getId(), e.getCode(), e.getName(), e.getVersion());
    }
}
