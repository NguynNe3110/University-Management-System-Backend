package com.university.reporting.internal.application;

import com.university.reporting.api.ReportingService;
import com.university.reporting.api.UniversityStatisticsView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {

    @Override
    public UniversityStatisticsView getOverviewStatistics() {
        return new UniversityStatisticsView(0L, 0L, 0L);
    }
}
