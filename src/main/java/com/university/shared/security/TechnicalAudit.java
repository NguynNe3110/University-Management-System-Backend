package com.university.shared.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Append-only technical audit; workflow decisions remain in their owning module. */
@Component
public class TechnicalAudit {
    private final JdbcTemplate jdbc;

    public TechnicalAudit(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void record(String module, String objectId, String action, String detail) {
        jdbc.update(
                "INSERT INTO technical_audit(id, actor, module, object_id, action, detail) VALUES"
                        + " (?,?,?,?,?,?)",
                UUID.randomUUID().toString(),
                Access.username(),
                module,
                objectId,
                action,
                detail);
    }
}
