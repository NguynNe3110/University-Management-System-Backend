package com.university.teaching.internal.application;

import com.university.teaching.api.TeachingJournal;
import com.university.teaching.api.TeachingLogView;
import com.university.teaching.internal.persistence.TeachingLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TeachingJournalImpl implements TeachingJournal {

    private final TeachingLogRepository teachingLogRepository;

    public TeachingJournalImpl(TeachingLogRepository teachingLogRepository) {
        this.teachingLogRepository = teachingLogRepository;
    }

    @Override
    public Optional<TeachingLogView> findByTimetableSessionId(String timetableSessionId) {
        return teachingLogRepository.findByTimetableSessionId(timetableSessionId)
            .map(t -> new TeachingLogView(t.getId(), t.getTimetableSessionId(), t.getLecturerId(), t.getActualHours(), t.getContentSummary(), t.getStatus()));
    }
}
