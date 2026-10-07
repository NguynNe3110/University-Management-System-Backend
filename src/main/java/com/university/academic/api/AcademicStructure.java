package com.university.academic.api;

import java.util.*;

public interface AcademicStructure {
    Optional<AdministrativeClassView> findAdministrativeClass(String id);

    CurriculumView curriculum(String programId);
}
