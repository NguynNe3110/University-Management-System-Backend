package com.university.academic.api;

public record ProgramView(String id, String code, String name, String departmentId, long version) {
    public ProgramView(String id, String code, String name, String departmentId) {
        this(id, code, name, departmentId, 0);
    }
}
