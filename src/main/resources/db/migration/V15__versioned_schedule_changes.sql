CREATE TABLE schedule_change (
 id VARCHAR(36) PRIMARY KEY,timetable_session_id VARCHAR(36) NOT NULL REFERENCES timetable_session(id),cancel BOOLEAN NOT NULL,
 room_id VARCHAR(36),starts_at TIMESTAMPTZ,ends_at TIMESTAMPTZ,reason TEXT NOT NULL,proposed_by VARCHAR(64) NOT NULL,
 expected_version BIGINT NOT NULL,status VARCHAR(32) NOT NULL,decided_by VARCHAR(64),decision_reason TEXT,decided_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(cancel OR (room_id IS NOT NULL AND starts_at IS NOT NULL AND ends_at>starts_at))
);
CREATE TABLE timetable_history (
 id VARCHAR(36) PRIMARY KEY,timetable_session_id VARCHAR(36) NOT NULL,version BIGINT NOT NULL,room_id VARCHAR(36) NOT NULL,
 starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,status VARCHAR(32) NOT NULL,change_id VARCHAR(36) NOT NULL REFERENCES schedule_change(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(timetable_session_id,version)
);
