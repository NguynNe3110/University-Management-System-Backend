package com.university.course.internal.web;

import com.university.course.internal.application.CourseFailure;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CourseController.class)
public class CourseExceptionHandler {
    @ExceptionHandler(CourseFailure.class)
    public ProblemDetail business(CourseFailure failure) {
        var status = failure.reason() == CourseFailure.Reason.NOT_FOUND ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return problem(status, failure.reason().name(), failure.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalid(IllegalArgumentException failure) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_COURSE", failure.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail invalidParameter() {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Invalid pagination parameters");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail stale() {
        return problem(HttpStatus.CONFLICT, "STALE_VERSION", "Course has changed; reload before updating");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrity(DataIntegrityViolationException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException constraint
                    && "uk_course_code".equals(constraint.getConstraintName())) {
                return problem(HttpStatus.CONFLICT, "DUPLICATE_CODE", "Course code already exists");
            }
        }
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "PERSISTENCE_ERROR", "Unable to save course");
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        var result = ProblemDetail.forStatusAndDetail(status, detail);
        result.setProperty("code", code);
        return result;
    }
}
