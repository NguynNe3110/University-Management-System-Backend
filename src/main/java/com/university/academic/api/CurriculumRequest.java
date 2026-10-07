package com.university.academic.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CurriculumRequest(
        @Min(0) long version, @NotNull @Size(min = 1, max = 1000) List<@Valid Course> courses) {
    public record Course(
            @NotBlank @Size(max = 36) String courseId,
            @NotNull @Size(max = 100)
                    List<@NotBlank @Size(max = 36) String> prerequisiteCourseIds) {}
}
