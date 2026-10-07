ALTER TABLE student_grade ADD COLUMN result_revision BIGINT NOT NULL DEFAULT 0;
CREATE TABLE grade_correction (
 id VARCHAR(36) PRIMARY KEY,teaching_class_id VARCHAR(36) NOT NULL,student_id VARCHAR(36) NOT NULL,
 attendance_score DOUBLE PRECISION NOT NULL CHECK(attendance_score BETWEEN 0 AND 10),
 midterm_score DOUBLE PRECISION NOT NULL CHECK(midterm_score BETWEEN 0 AND 10),final_score DOUBLE PRECISION NOT NULL CHECK(final_score BETWEEN 0 AND 10),
 reason TEXT NOT NULL,proposed_by VARCHAR(64) NOT NULL,expected_revision BIGINT NOT NULL,status VARCHAR(32) NOT NULL,
 decided_by VARCHAR(64),decision_reason TEXT,decided_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE grade_result_history (
 id VARCHAR(36) PRIMARY KEY,grade_id VARCHAR(36) NOT NULL,result_revision BIGINT NOT NULL,
 attendance_score DOUBLE PRECISION,midterm_score DOUBLE PRECISION,final_score DOUBLE PRECISION,total_score DOUBLE PRECISION,
 correction_id VARCHAR(36) NOT NULL REFERENCES grade_correction(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(grade_id,result_revision)
);
CREATE TABLE attendance_correction (
 id VARCHAR(36) PRIMARY KEY,attendance_session_id VARCHAR(36) NOT NULL REFERENCES attendance_session(id),student_id VARCHAR(36) NOT NULL,
 requested_status VARCHAR(32) NOT NULL CHECK(requested_status IN ('PRESENT','ABSENT','EXCUSED')),original_status VARCHAR(32) NOT NULL,
 reason TEXT NOT NULL,proposed_by VARCHAR(64) NOT NULL,status VARCHAR(32) NOT NULL,decided_by VARCHAR(64),decision_reason TEXT,
 decided_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE attendance_result_history (
 id VARCHAR(36) PRIMARY KEY,attendance_record_id VARCHAR(36) NOT NULL REFERENCES attendance_record(id),previous_status VARCHAR(32) NOT NULL,
 correction_id VARCHAR(36) NOT NULL REFERENCES attendance_correction(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
