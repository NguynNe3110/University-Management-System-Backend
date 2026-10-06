package com.university.course.internal.application;

public class CourseFailure extends RuntimeException {
    public enum Reason { NOT_FOUND, DUPLICATE_CODE, STALE_VERSION, ARCHIVED }
    private final Reason reason;

    public CourseFailure(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
