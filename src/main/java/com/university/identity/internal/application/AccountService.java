package com.university.identity.internal.application;

import com.university.identity.api.*;
import com.university.identity.internal.domain.AppUser;
import com.university.identity.internal.persistence.AppUserRepository;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class AccountService {
    public static final Set<String> ROLES =
            Set.of(
                    "ADMIN",
                    "ACADEMIC_STAFF",
                    "ACADEMIC_APPROVER",
                    "ROOM_MANAGER",
                    "STUDENT",
                    "LECTURER",
                    "FACULTY_STAFF",
                    "EXAM_STAFF",
                    "FINANCE_STAFF",
                    "FINANCE_APPROVER",
                    "REPORT_VIEWER");
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;
    private final TechnicalAudit audit;

    public AccountService(
            AppUserRepository users,
            PasswordEncoder encoder,
            JdbcTemplate jdbc,
            TechnicalAudit audit) {
        this.users = users;
        this.encoder = encoder;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    public UserProfileView create(AccountRequest r) {
        Access.require("ADMIN", "GLOBAL", "*");
        if (users.findByUsername(r.username()).isPresent())
            throw BusinessException.conflict("Username already exists");
        validate(r.grants());
        var u =
                new AppUser(
                        UUID.randomUUID().toString(),
                        r.username(),
                        encoder.encode(r.password()),
                        r.fullName().trim(),
                        r.email().trim(),
                        roles(r.grants()),
                        "ACTIVE");
        u.link(r.studentId(), r.lecturerId());
        users.saveAndFlush(u);
        saveGrants(u.getId(), r.grants());
        audit.record(
                "identity",
                u.getId(),
                "CREATE_ACCOUNT",
                "Initial password supplied by administrator; hash stored only");
        return view(u);
    }

    public UserProfileView status(String id, String status) {
        Access.require("ADMIN", "GLOBAL", "*");
        if (!Set.of("ACTIVE", "LOCKED").contains(status))
            throw new IllegalArgumentException("Status must be ACTIVE or LOCKED");
        var u = users.findById(id).orElseThrow(() -> BusinessException.missing("Account"));
        if (Access.self("USER", id))
            throw BusinessException.conflict("Cannot change your own account status");
        u.setStatus(status);
        audit.record("identity", id, "ACCOUNT_STATUS", status);
        return view(u);
    }

    public void grants(String id, List<AccountRequest.Grant> grants) {
        Access.require("ADMIN", "GLOBAL", "*");
        validate(grants);
        var u = users.findById(id).orElseThrow(() -> BusinessException.missing("Account"));
        if (Access.self("USER", id))
            throw BusinessException.conflict("Cannot change your own grants");
        jdbc.update("DELETE FROM account_grant WHERE user_id=?", id);
        saveGrants(id, grants);
        u.setRoles(roles(grants));
        audit.record("identity", id, "REPLACE_GRANTS", grants.toString());
    }

    public void password(String current, String replacement) {
        var u =
                users.findByUsername(Access.username())
                        .orElseThrow(() -> BusinessException.missing("Account"));
        if (!encoder.matches(current, u.getPasswordHash()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Incorrect current password");
        u.setPasswordHash(encoder.encode(replacement));
        audit.record("identity", u.getId(), "CHANGE_PASSWORD", "Password changed");
    }

    private void validate(List<AccountRequest.Grant> grants) {
        if (grants == null || grants.isEmpty() || grants.size() > 20)
            throw new IllegalArgumentException("Supply 1 to 20 grants");
        for (var g : grants) {
            if (!ROLES.contains(g.role())
                    || !Set.of("GLOBAL", "DEPARTMENT", "CLASS", "STUDENT", "SEMESTER", "ROOM")
                            .contains(g.scopeType()))
                throw new IllegalArgumentException("Unknown role or scope type");
            if (!g.validUntil().isAfter(g.validFrom()))
                throw new IllegalArgumentException("Invalid grant validity interval");
            if (g.scopeId().contains("|")
                    || (g.scopeType().equals("GLOBAL") && !g.scopeId().equals("*")))
                throw new IllegalArgumentException("Invalid scope ID");
            if (Set.of("ADMIN", "STUDENT", "LECTURER").contains(g.role())
                    && !g.scopeType().equals("GLOBAL"))
                throw new IllegalArgumentException(
                        "Personal or technical roles use GLOBAL; business assignment still checked"
                                + " separately");
        }
    }

    private String roles(List<AccountRequest.Grant> grants) {
        return String.join(
                ",", grants.stream().map(AccountRequest.Grant::role).distinct().sorted().toList());
    }

    private void saveGrants(String id, List<AccountRequest.Grant> grants) {
        for (var g : grants)
            jdbc.update(
                    "INSERT INTO"
                        + " account_grant(id,user_id,role,scope_type,scope_id,valid_from,valid_until)"
                        + " VALUES (?,?,?,?,?,?,?)",
                    UUID.randomUUID().toString(),
                    id,
                    g.role(),
                    g.scopeType(),
                    g.scopeId(),
                    Timestamp.from(g.validFrom()),
                    Timestamp.from(g.validUntil()));
    }

    private UserProfileView view(AppUser u) {
        return new UserProfileView(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRoles(),
                u.getStatus());
    }
}
