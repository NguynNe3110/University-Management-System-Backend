package com.university.teachingclass.api;

import java.util.List;
import java.util.Optional;

public interface TeachingClassDirectory {
    Optional<TeachingClassView> findClassById(String id);
    List<TeachingClassView> findClassesBySemester(String semesterId);
    List<TeachingClassView> findAllClasses();
    int getCapacity(String classId);
}
