package com.university.course.internal.application;

import com.university.course.api.CourseCatalog;
import com.university.course.api.CourseView;
import com.university.course.internal.domain.Course;
import com.university.course.internal.persistence.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;
import static com.university.course.internal.application.CourseFailure.Reason.*;

@Service
@Transactional(readOnly = true)
public class CourseService implements CourseCatalog {
    private final CourseRepository repository;

    public CourseService(CourseRepository repository) { this.repository = repository; }

    @Override
    public Optional<CourseView> findById(UUID id) {
        return repository.findById(id).map(CourseService::view);
    }

    public CourseView get(UUID id) { return view(require(id)); }

    public Page<CourseView> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("code"))).map(CourseService::view);
    }

    @Transactional
    public CourseView create(String code, String name, int credits) {
        var course = new Course(code, name, credits);
        if (repository.existsByCode(course.getCode())) {
            throw new CourseFailure(DUPLICATE_CODE, "Course code already exists");
        }
        // Unique constraint is authoritative when concurrent requests use the same code.
        return view(repository.saveAndFlush(course));
    }

    @Transactional
    public CourseView update(UUID id, String name, int credits, long expectedVersion) {
        var course = require(id);
        checkVersion(course, expectedVersion);
        if (!course.isActive()) throw new CourseFailure(ARCHIVED, "Archived course cannot be edited");
        course.update(name, credits);
        repository.flush();
        return view(course);
    }

    @Transactional
    public CourseView archive(UUID id, long expectedVersion) {
        var course = require(id);
        checkVersion(course, expectedVersion);
        course.archive();
        repository.flush();
        return view(course);
    }

    private Course require(UUID id) {
        return repository.findById(id).orElseThrow(() -> new CourseFailure(NOT_FOUND, "Course not found"));
    }

    private static void checkVersion(Course course, long expectedVersion) {
        if (course.getVersion() != expectedVersion) {
            throw new CourseFailure(STALE_VERSION, "Course has changed; reload before updating");
        }
    }

    private static CourseView view(Course course) {
        return new CourseView(course.getId(), course.getCode(), course.getName(), course.getCredits(),
                course.isActive(), course.getVersion());
    }
}
