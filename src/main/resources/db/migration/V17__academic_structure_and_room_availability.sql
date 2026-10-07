CREATE TABLE cohort (id VARCHAR(36) PRIMARY KEY,code VARCHAR(32) NOT NULL UNIQUE,admission_year INTEGER NOT NULL CHECK(admission_year BETWEEN 1900 AND 9999));
CREATE TABLE administrative_class (id VARCHAR(36) PRIMARY KEY,code VARCHAR(32) NOT NULL UNIQUE,program_id VARCHAR(36) NOT NULL REFERENCES program(id),cohort_id VARCHAR(36) NOT NULL REFERENCES cohort(id));
ALTER TABLE student_profile ADD COLUMN administrative_class_id VARCHAR(36) REFERENCES administrative_class(id);
CREATE TABLE curriculum_config (program_id VARCHAR(36) PRIMARY KEY REFERENCES program(id),version BIGINT NOT NULL);
CREATE TABLE curriculum_course (program_id VARCHAR(36) NOT NULL REFERENCES program(id),course_id VARCHAR(36) NOT NULL,prerequisites TEXT NOT NULL,PRIMARY KEY(program_id,course_id));
CREATE TABLE curriculum_history (program_id VARCHAR(36) NOT NULL,version BIGINT NOT NULL,course_id VARCHAR(36) NOT NULL,prerequisites TEXT NOT NULL,changed_by VARCHAR(64) NOT NULL,changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(program_id,version,course_id));
CREATE TABLE room_block (id VARCHAR(36) PRIMARY KEY,room_id VARCHAR(36) NOT NULL REFERENCES room(id),starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,reason TEXT NOT NULL,CHECK(ends_at>starts_at));
CREATE INDEX ix_room_block_interval ON room_block(room_id,starts_at,ends_at);
