package com.university.reporting.internal.application;

import com.university.course.api.CourseCatalog;
import com.university.reporting.api.*;
import com.university.shared.security.Access;
import com.university.student.api.StudentDirectory;
import com.university.teachingclass.api.TeachingClassDirectory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {
    private final CourseCatalog courses;
    private final StudentDirectory students;
    private final TeachingClassDirectory classes;

    public ReportingServiceImpl(
            CourseCatalog courses, StudentDirectory students, TeachingClassDirectory classes) {
        this.courses = courses;
        this.students = students;
        this.classes = classes;
    }

    @Override
    public UniversityStatisticsView getOverviewStatistics() {
        Access.require("REPORT_VIEWER", "GLOBAL", "*");
        return new UniversityStatisticsView(
                courses.countCourses(),
                students.findAllStudents().size(),
                classes.findAllClasses().size());
    }
}
