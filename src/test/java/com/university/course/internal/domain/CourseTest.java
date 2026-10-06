package com.university.course.internal.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CourseTest {
    @Test
    void normalizesCodeAndName() {
        var course = new Course("  cs101 ", "  Tin học cơ sở  ", 3);
        assertThat(course.getCode()).isEqualTo("CS101");
        assertThat(course.getName()).isEqualTo("Tin học cơ sở");
    }

    @Test
    void refusesInvalidCodeAndCredits() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Course("CS 101", "Tin học", 3));
        assertThatIllegalArgumentException().isThrownBy(() -> new Course("CS101", "Tin học", 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new Course("CS101", "Tin học", 31));
    }

    @Test
    void archivalPreservesIdentityAndBlocksEditing() {
        var course = new Course("CS101", "Tin học", 3);
        var id = course.getId();
        course.archive();
        assertThat(course.isActive()).isFalse();
        assertThat(course.getId()).isEqualTo(id);
        assertThatIllegalStateException().isThrownBy(() -> course.update("Tên mới", 4));
    }
}
