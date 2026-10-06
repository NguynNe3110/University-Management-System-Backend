package com.university.course.internal.web;

import org.springframework.context.annotation.Profile;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets the manual demo client obtain CSRF protection for HTTP Basic write requests. */
@RestController
@Profile("course-demo")
public class CourseDemoCsrfController {
    @GetMapping("/api/v1/demo/csrf")
    public CsrfView csrf(CsrfToken token) {
        return new CsrfView(token.getHeaderName(), token.getToken());
    }

    public record CsrfView(String headerName, String token) {}
}
