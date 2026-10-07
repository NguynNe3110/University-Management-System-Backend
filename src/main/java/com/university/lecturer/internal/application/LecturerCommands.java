package com.university.lecturer.internal.application;

import com.university.lecturer.api.*;
import com.university.lecturer.internal.domain.LecturerProfile;
import com.university.lecturer.internal.persistence.LecturerProfileRepository;
import com.university.organization.api.OrganizationDirectory;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class LecturerCommands {
    private final LecturerProfileRepository repository;
    private final TechnicalAudit audit;
    private final OrganizationDirectory organization;

    public LecturerCommands(
            LecturerProfileRepository repository,
            TechnicalAudit audit,
            OrganizationDirectory organization) {
        this.repository = repository;
        this.audit = audit;
        this.organization = organization;
    }

    public LecturerProfileView create(LecturerProfileRequest r) {
        if (organization.findDepartmentById(r.departmentId()).isEmpty())
            throw BusinessException.missing("Department");
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", r.departmentId());
        var e =
                new LecturerProfile(
                        UUID.randomUUID().toString(),
                        r.lecturerCode(),
                        r.fullName(),
                        r.email(),
                        r.departmentId(),
                        r.status());
        repository.saveAndFlush(e);
        audit.record("lecturer", e.getId(), "CREATE_LECTURER", r.toString());
        return view(e);
    }

    public LecturerProfileView update(String id, LecturerProfileRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Lecturer"));
        if (organization.findDepartmentById(r.departmentId()).isEmpty())
            throw BusinessException.missing("Department");
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", e.getDepartmentId());
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", r.departmentId());
        e.revise(
                r.lecturerCode(),
                r.fullName(),
                r.email(),
                r.departmentId(),
                r.status(),
                r.version());
        repository.flush();
        audit.record("lecturer", id, "UPDATE_LECTURER", r.toString());
        return view(e);
    }

    private LecturerProfileView view(LecturerProfile e) {
        return new LecturerProfileView(
                e.getId(),
                e.getLecturerCode(),
                e.getFullName(),
                e.getEmail(),
                e.getDepartmentId(),
                e.getStatus(),
                e.getVersion());
    }
}
