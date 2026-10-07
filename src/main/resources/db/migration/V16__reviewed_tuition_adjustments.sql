CREATE TABLE fee_adjustment (
 id VARCHAR(36) PRIMARY KEY,tuition_fee_id VARCHAR(36) NOT NULL REFERENCES tuition_fee(id),delta NUMERIC(12,2) NOT NULL CHECK(delta<>0),
 reason TEXT NOT NULL,proposed_by VARCHAR(64) NOT NULL,status VARCHAR(32) NOT NULL,
 decided_by VARCHAR(64),decision_reason TEXT,decided_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE tuition_fee ADD CONSTRAINT ck_nonnegative_tuition CHECK(amount_due>=0 AND amount_paid>=0);
