package com.university.attendance.internal.persistence;

import com.university.attendance.internal.domain.AttendanceSession;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, String> {
    Optional<AttendanceSession> findBySessionId(String sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AttendanceSession s where s.id=:id")
    Optional<AttendanceSession> lockById(@Param("id") String id);
}
