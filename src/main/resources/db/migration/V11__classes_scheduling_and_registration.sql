ALTER TABLE teaching_class ADD COLUMN department_id VARCHAR(36) REFERENCES department(id);
ALTER TABLE teaching_class ADD COLUMN tuition_rate NUMERIC(12,2) CHECK(tuition_rate>=0);
ALTER TABLE teaching_class ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE timetable_session ADD COLUMN starts_at TIMESTAMPTZ;
ALTER TABLE timetable_session ADD COLUMN ends_at TIMESTAMPTZ;
ALTER TABLE timetable_session ADD COLUMN created_by VARCHAR(64);
ALTER TABLE timetable_session ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE timetable_session ADD CONSTRAINT ck_session_time CHECK(ends_at>starts_at);
CREATE TABLE scheduling_lock (id INTEGER PRIMARY KEY);
INSERT INTO scheduling_lock VALUES(1);
CREATE TABLE registration_window (
    id VARCHAR(36) PRIMARY KEY, semester_id VARCHAR(36) NOT NULL REFERENCES semester(id),
    program_id VARCHAR(36) NOT NULL REFERENCES program(id), opens_at TIMESTAMPTZ NOT NULL,
    closes_at TIMESTAMPTZ NOT NULL, cancellation_deadline TIMESTAMPTZ NOT NULL,
    max_credits INTEGER NOT NULL CHECK(max_credits>0), prerequisites TEXT NOT NULL,
    minimum_passing_score DOUBLE PRECISION NOT NULL CHECK(minimum_passing_score BETWEEN 0 AND 10),
    CHECK(closes_at>opens_at),CHECK(cancellation_deadline>=opens_at)
);
ALTER TABLE student_enrollment ADD COLUMN window_id VARCHAR(36) REFERENCES registration_window(id);
ALTER TABLE student_enrollment ADD COLUMN cancelled_at TIMESTAMPTZ;
CREATE INDEX ix_enrollment_class_status ON student_enrollment(teaching_class_id,status);
CREATE INDEX ix_enrollment_student ON student_enrollment(student_id);
