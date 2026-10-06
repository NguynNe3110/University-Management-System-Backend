package com.university.course.api;

import java.util.UUID;

/** Immutable contract; callers never receive the JPA entity. */
public record CourseView(UUID id, String code, String name, int credits, boolean active, long version) {}
