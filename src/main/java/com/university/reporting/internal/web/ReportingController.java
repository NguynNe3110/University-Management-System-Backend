package com.university.reporting.internal.web;

import com.university.reporting.api.ReportingService;
import com.university.reporting.api.UniversityStatisticsView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/overview")
    public ResponseEntity<UniversityStatisticsView> getOverview() {
        return ResponseEntity.ok(reportingService.getOverviewStatistics());
    }
}
