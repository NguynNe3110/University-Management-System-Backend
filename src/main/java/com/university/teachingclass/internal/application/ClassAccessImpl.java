package com.university.teachingclass.internal.application;

import com.university.shared.exception.BusinessException;
import com.university.shared.security.Access;
import com.university.teachingclass.api.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ClassAccessImpl implements ClassAccess {
    private final TeachingClassDirectory classes;

    public ClassAccessImpl(TeachingClassDirectory classes) {
        this.classes = classes;
    }

    @Override
    public boolean canRead(String id) {
        var c = classes.findClassById(id).orElseThrow(() -> BusinessException.missing("Class"));
        return Access.self("LECTURER", c.lecturerId()) && Access.authority("ROLE_LECTURER")
                || Access.can("ACADEMIC_STAFF", "SEMESTER", c.semesterId())
                || Access.can("ACADEMIC_STAFF", "DEPARTMENT", c.departmentId())
                || Access.can("FACULTY_STAFF", "DEPARTMENT", c.departmentId())
                || scopedRead("EXAM_STAFF", c)
                || scopedRead("ACADEMIC_APPROVER", c);
    }

    private boolean scopedRead(String role, TeachingClassView c) {
        return Access.can(role, "CLASS", c.id())
                || Access.can(role, "DEPARTMENT", c.departmentId())
                || Access.can(role, "SEMESTER", c.semesterId());
    }

    @Override
    public void requireLecturer(String id) {
        var c = classes.findClassById(id).orElseThrow(() -> BusinessException.missing("Class"));
        if (!Access.authority("ROLE_LECTURER") || !Access.self("LECTURER", c.lecturerId()))
            throw new AccessDeniedException("Not the assigned lecturer");
    }

    @Override
    public void requireStaff(String role, String id) {
        var c = classes.findClassById(id).orElseThrow(() -> BusinessException.missing("Class"));
        if (!Access.can(role, "CLASS", id)
                && !Access.can(role, "DEPARTMENT", c.departmentId())
                && !Access.can(role, "SEMESTER", c.semesterId()))
            throw new AccessDeniedException("Class outside assigned scope");
    }
}
