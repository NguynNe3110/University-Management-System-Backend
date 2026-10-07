ALTER TABLE app_user ADD COLUMN student_id VARCHAR(36) UNIQUE REFERENCES student_profile(id);
ALTER TABLE app_user ADD COLUMN lecturer_id VARCHAR(36) UNIQUE REFERENCES lecturer_profile(id);
CREATE TABLE account_grant (
    id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL REFERENCES app_user(id),
    role VARCHAR(32) NOT NULL, scope_type VARCHAR(32) NOT NULL, scope_id VARCHAR(36) NOT NULL,
    valid_from TIMESTAMPTZ NOT NULL, valid_until TIMESTAMPTZ NOT NULL,
    CHECK(valid_until>valid_from), UNIQUE(user_id,role,scope_type,scope_id)
);
CREATE INDEX ix_grant_user ON account_grant(user_id);
CREATE TABLE technical_audit (
    id VARCHAR(36) PRIMARY KEY, actor VARCHAR(64) NOT NULL, module VARCHAR(64) NOT NULL,
    object_id VARCHAR(36) NOT NULL, action VARCHAR(64) NOT NULL, detail TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_audit_object ON technical_audit(module,object_id,created_at);
