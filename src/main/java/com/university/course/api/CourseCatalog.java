package com.university.course.api;

import java.util.Optional;
import java.util.UUID;

/** Read facade for academic and teachingclass. Authorization belongs to the calling use case. */
public interface CourseCatalog {
    Optional<CourseView> findById(UUID id);
}
