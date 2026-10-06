package com.university.course.internal.application;

import com.university.course.internal.domain.Course;
import com.university.course.internal.persistence.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock CourseRepository repository;
    @InjectMocks CourseService service;

    @Test
    void staleClientCannotOverwriteExistingCourse() {
        var course = new Course("CS101", "Tin học", 3);
        when(repository.findById(course.getId())).thenReturn(Optional.of(course));
        assertThatThrownBy(() -> service.update(course.getId(), "Ghi đè", 4, 99))
                .isInstanceOfSatisfying(CourseFailure.class,
                        failure -> assertThat(failure.reason()).isEqualTo(CourseFailure.Reason.STALE_VERSION));
        assertThat(course.getName()).isEqualTo("Tin học");
        verify(repository, never()).flush();
    }

    @Test
    void duplicateNormalizedCodeIsRejected() {
        when(repository.existsByCode("CS101")).thenReturn(true);
        assertThatThrownBy(() -> service.create(" cs101 ", "Tin học", 3))
                .isInstanceOfSatisfying(CourseFailure.class,
                        failure -> assertThat(failure.reason()).isEqualTo(CourseFailure.Reason.DUPLICATE_CODE));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void missingCourseIsReportedExplicitly() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOfSatisfying(CourseFailure.class,
                failure -> assertThat(failure.reason()).isEqualTo(CourseFailure.Reason.NOT_FOUND));
        assertThat(service.findById(id)).isEmpty();
    }
}
