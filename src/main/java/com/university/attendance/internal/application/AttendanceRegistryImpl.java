package com.university.attendance.internal.application;

import com.university.attendance.api.AttendanceRecordView;
import com.university.attendance.api.AttendanceRegistry;
import com.university.attendance.internal.persistence.AttendanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AttendanceRegistryImpl implements AttendanceRegistry {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRegistryImpl(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    public Optional<AttendanceRecordView> findById(String id) {
        return attendanceRecordRepository.findById(id)
            .map(a -> new AttendanceRecordView(a.getId(), a.getAttendanceSessionId(), a.getStudentId(), a.getCheckInTime(), a.getStatus()));
    }

    @Override
    public List<AttendanceRecordView> findBySessionId(String sessionId) {
        return attendanceRecordRepository.findByAttendanceSessionId(sessionId)
            .stream()
            .map(a -> new AttendanceRecordView(a.getId(), a.getAttendanceSessionId(), a.getStudentId(), a.getCheckInTime(), a.getStatus()))
            .toList();
    }
}
