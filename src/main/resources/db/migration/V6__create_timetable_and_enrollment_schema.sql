CREATE TABLE IF NOT EXISTS timetable_session (
    id VARCHAR(36) PRIMARY KEY,
    teaching_class_id VARCHAR(36) NOT NULL,
    session_number INT NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    session_date DATE NOT NULL,
    start_period INT NOT NULL,
    end_period INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS student_enrollment (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    teaching_class_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL,
    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_student_class UNIQUE(student_id, teaching_class_id)
);
