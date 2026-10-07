CREATE TABLE tuition_line (
 id VARCHAR(36) PRIMARY KEY,tuition_fee_id VARCHAR(36) NOT NULL REFERENCES tuition_fee(id),course_id VARCHAR(36) NOT NULL,
 enrollment_id VARCHAR(36) NOT NULL,amount NUMERIC(12,2) NOT NULL CHECK(amount>=0),UNIQUE(tuition_fee_id,course_id)
);
CREATE TABLE payment_request (
 id VARCHAR(36) PRIMARY KEY,tuition_fee_id VARCHAR(36) NOT NULL REFERENCES tuition_fee(id),request_key VARCHAR(64) NOT NULL,
 amount NUMERIC(12,2) NOT NULL CHECK(amount>0),currency VARCHAR(3) NOT NULL,status VARCHAR(32) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(tuition_fee_id,request_key)
);
CREATE TABLE payment_transaction (
 id VARCHAR(36) PRIMARY KEY,provider VARCHAR(32) NOT NULL,transaction_id VARCHAR(64) NOT NULL,
 payment_request_id VARCHAR(36) NOT NULL REFERENCES payment_request(id),amount NUMERIC(12,2) NOT NULL CHECK(amount>0),
 currency VARCHAR(3) NOT NULL,outcome VARCHAR(32) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(provider,transaction_id)
);
