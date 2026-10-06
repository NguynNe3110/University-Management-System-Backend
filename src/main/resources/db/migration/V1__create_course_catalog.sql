CREATE SCHEMA course;

CREATE TABLE course.courses (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    credits INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_course_code UNIQUE (code),
    CONSTRAINT ck_course_code CHECK (code ~ '^[A-Z0-9][A-Z0-9_-]{0,29}$'),
    CONSTRAINT ck_course_name CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_course_credits CHECK (credits BETWEEN 1 AND 30)
);
