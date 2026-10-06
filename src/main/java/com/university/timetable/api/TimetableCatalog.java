package com.university.timetable.api;

import java.util.List;
import java.util.Optional;

public interface TimetableCatalog {
    Optional<TimetableSessionView> findSessionById(String sessionId);
    List<TimetableSessionView> findSessionsByTeachingClassId(String teachingClassId);
}
