ALTER TABLE attendance_session ADD CONSTRAINT uq_attendance_timetable UNIQUE(session_id);
CREATE TABLE attendance_roster (
 attendance_session_id VARCHAR(36) NOT NULL REFERENCES attendance_session(id),student_id VARCHAR(36) NOT NULL,
 PRIMARY KEY(attendance_session_id,student_id)
);
ALTER TABLE teaching_log ADD COLUMN submitted_by VARCHAR(64);
ALTER TABLE teaching_log ADD COLUMN decided_by VARCHAR(64);
ALTER TABLE teaching_log ADD COLUMN decision_reason TEXT;
CREATE TABLE grade_batch (
 teaching_class_id VARCHAR(36) PRIMARY KEY,attendance_weight DOUBLE PRECISION NOT NULL,
 midterm_weight DOUBLE PRECISION NOT NULL,final_weight DOUBLE PRECISION NOT NULL,
 status VARCHAR(32) NOT NULL,submitted_by VARCHAR(64),published_by VARCHAR(64),version BIGINT NOT NULL DEFAULT 0,
 CHECK(attendance_weight BETWEEN 0 AND 1),CHECK(midterm_weight BETWEEN 0 AND 1),CHECK(final_weight BETWEEN 0 AND 1),
 CHECK(abs(attendance_weight+midterm_weight+final_weight-1)<0.000001)
);
