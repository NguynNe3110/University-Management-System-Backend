package com.university.lecturer.api;

public record LecturerProfileView(
        String id,
        String lecturerCode,
        String fullName,
        String email,
        String departmentId,
        String status,
        long version) {
    public LecturerProfileView(
            String id,
            String lecturerCode,
            String fullName,
            String email,
            String departmentId,
            String status) {
        this(id, lecturerCode, fullName, email, departmentId, status, 0);
    }
}
