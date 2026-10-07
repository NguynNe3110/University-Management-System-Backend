package com.university.timetable.internal.application;

import com.university.timetable.api.TimetableCatalog;
import com.university.timetable.api.TimetableSessionView;
import com.university.timetable.internal.persistence.TimetableSessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TimetableCatalogImpl implements TimetableCatalog {

    private final TimetableSessionRepository timetableSessionRepository;

    public TimetableCatalogImpl(TimetableSessionRepository timetableSessionRepository) {
        this.timetableSessionRepository = timetableSessionRepository;
    }

    @Override
    public Optional<TimetableSessionView> findSessionById(String sessionId) {
        return timetableSessionRepository
                .findById(sessionId)
                .map(
                        s ->
                                new TimetableSessionView(
                                        s.getId(),
                                        s.getTeachingClassId(),
                                        s.getSessionNumber(),
                                        s.getRoomId(),
                                        s.getSessionDate(),
                                        s.getStartPeriod(),
                                        s.getEndPeriod(),
                                        s.getStatus(),
                                        s.getStartsAt(),
                                        s.getEndsAt(),
                                        s.getVersion()));
    }

    @Override
    public List<TimetableSessionView> findSessionsByTeachingClassId(String teachingClassId) {
        return timetableSessionRepository
                .findByTeachingClassIdOrderBySessionNumberAsc(teachingClassId)
                .stream()
                .map(
                        s ->
                                new TimetableSessionView(
                                        s.getId(),
                                        s.getTeachingClassId(),
                                        s.getSessionNumber(),
                                        s.getRoomId(),
                                        s.getSessionDate(),
                                        s.getStartPeriod(),
                                        s.getEndPeriod(),
                                        s.getStatus(),
                                        s.getStartsAt(),
                                        s.getEndsAt(),
                                        s.getVersion()))
                .toList();
    }
}
