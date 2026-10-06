CREATE TABLE IF NOT EXISTS attendance_session (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    qr_token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance_record (
    id VARCHAR(36) PRIMARY KEY,
    attendance_session_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    check_in_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_att_session_student UNIQUE(attendance_session_id, student_id)
);

CREATE TABLE IF NOT EXISTS teaching_log (
    id VARCHAR(36) PRIMARY KEY,
    timetable_session_id VARCHAR(36) NOT NULL UNIQUE,
    lecturer_id VARCHAR(36) NOT NULL,
    actual_hours INT NOT NULL,
    content_summary TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tuition_fee (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    semester_id VARCHAR(36) NOT NULL,
    amount_due NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_tuition_student_semester UNIQUE(student_id, semester_id)
);

CREATE TABLE IF NOT EXISTS student_grade (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    teaching_class_id VARCHAR(36) NOT NULL,
    attendance_score DOUBLE PRECISION,
    midterm_score DOUBLE PRECISION,
    final_score DOUBLE PRECISION,
    total_score DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_grade_student_class UNIQUE(student_id, teaching_class_id)
);
