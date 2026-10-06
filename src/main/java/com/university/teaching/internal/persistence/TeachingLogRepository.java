package com.university.teaching.internal.persistence;

import com.university.teaching.internal.domain.TeachingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeachingLogRepository extends JpaRepository<TeachingLog, String> {
    Optional<TeachingLog> findByTimetableSessionId(String timetableSessionId);
}
