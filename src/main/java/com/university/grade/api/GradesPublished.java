package com.university.grade.api;

import java.util.List;

public record GradesPublished(String classId, List<String> studentIds) {}
