package com.university.teaching.api;

import java.util.Optional;

public interface TeachingJournal {
    Optional<TeachingLogView> findByTimetableSessionId(String timetableSessionId);
}
