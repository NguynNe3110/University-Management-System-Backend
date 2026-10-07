package com.university.teachingclass.api;

public interface ClassAccess {
    boolean canRead(String classId);

    void requireLecturer(String classId);

    void requireStaff(String role, String classId);
}
