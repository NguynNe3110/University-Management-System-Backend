package com.university.attendance.api;

import java.util.List;
import java.util.Optional;

public interface AttendanceRegistry {
    Optional<AttendanceRecordView> findById(String id);
    List<AttendanceRecordView> findBySessionId(String sessionId);
}
