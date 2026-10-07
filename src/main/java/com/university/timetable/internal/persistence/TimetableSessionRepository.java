package com.university.timetable.internal.persistence;

import com.university.timetable.internal.domain.TimetableSession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
            "select s from TimetableSession s where s.id=:id")
    java.util.Optional<TimetableSession> lockById(
            @org.springframework.data.repository.query.Param("id") String id);

    List<TimetableSession> findByTeachingClassIdOrderBySessionNumberAsc(String teachingClassId);
}
