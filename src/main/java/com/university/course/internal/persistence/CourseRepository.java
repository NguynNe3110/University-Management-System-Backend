package com.university.course.internal.persistence;

import com.university.course.internal.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/** Owned by course. Other modules must use CourseCatalog instead. */
public interface CourseRepository extends JpaRepository<Course, UUID> {
    boolean existsByCode(String code);
}
