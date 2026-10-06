package com.university.timetable.internal.persistence;

import com.university.timetable.internal.domain.TimetableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, String> {
    List<TimetableSession> findByTeachingClassIdOrderBySessionNumberAsc(String teachingClassId);
}
