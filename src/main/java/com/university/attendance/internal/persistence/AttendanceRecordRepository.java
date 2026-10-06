package com.university.attendance.internal.persistence;

import com.university.attendance.internal.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {
    List<AttendanceRecord> findByAttendanceSessionId(String attendanceSessionId);
}
