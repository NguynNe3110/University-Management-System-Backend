package com.university.academic.api;

import java.util.List;
import java.util.Optional;

public interface AcademicCatalog {
    Optional<ProgramView> findProgramById(String id);
    Optional<SemesterView> findSemesterById(String id);
    List<ProgramView> findAllPrograms();
    List<SemesterView> findAllSemesters();
}
