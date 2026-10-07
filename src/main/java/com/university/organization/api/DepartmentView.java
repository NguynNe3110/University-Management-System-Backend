package com.university.organization.api;

public record DepartmentView(String id, String code, String name, long version) {
    public DepartmentView(String id, String code, String name) {
        this(id, code, name, 0);
    }
}
