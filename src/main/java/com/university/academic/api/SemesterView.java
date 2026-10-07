package com.university.academic.api;

public record SemesterView(String id, String code, String academicYear, int term, long version) {
    public SemesterView(String id, String code, String academicYear, int term) {
        this(id, code, academicYear, term, 0);
    }
}
