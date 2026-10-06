CREATE TABLE IF NOT EXISTS teaching_class (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    course_id VARCHAR(36) NOT NULL,
    semester_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36),
    lecturer_id VARCHAR(36),
    max_capacity INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
