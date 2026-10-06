package com.university.lecturer.api;

import java.util.List;
import java.util.Optional;

public interface LecturerDirectory {
    Optional<LecturerProfileView> findByLecturerCode(String lecturerCode);
    Optional<LecturerProfileView> findById(String id);
    List<LecturerProfileView> findAllLecturers();
}
