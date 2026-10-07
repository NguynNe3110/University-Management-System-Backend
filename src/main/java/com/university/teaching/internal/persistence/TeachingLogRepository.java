package com.university.teaching.internal.persistence;

import com.university.teaching.internal.domain.TeachingLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeachingLogRepository extends JpaRepository<TeachingLog, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from TeachingLog t where t.id=:id")
    Optional<TeachingLog> lockById(
            @org.springframework.data.repository.query.Param("id") String id);

    Optional<TeachingLog> findByTimetableSessionId(String timetableSessionId);
}
