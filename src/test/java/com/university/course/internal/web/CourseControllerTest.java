package com.university.course.internal.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.university.course.api.CourseView;
import com.university.course.internal.application.CourseService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@WebMvcTest(CourseController.class)
@Import(CourseMethodSecurity.class)
class CourseControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CourseService service;
    private static final String BODY = "{\"code\":\"CS101\",\"name\":\"Tin hoc\",\"credits\":3}";

    @Test
    void anonymousCannotReadCatalog() throws Exception {
        mvc.perform(get("/api/v1/courses")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        mvc.perform(
                        post("/api/v1/courses")
                                .with(user("student").roles("STUDENT"))
                                .with(csrf())
                                .contentType("application/json")
                                .content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void academicStaffCanCreateCourse() throws Exception {
        var id = UUID.randomUUID();
        when(service.create("CS101", "Tin hoc", 3))
                .thenReturn(new CourseView(id, "CS101", "Tin hoc", 3, true, 0));
        mvc.perform(
                        post("/api/v1/courses")
                                .with(
                                        user("staff")
                                                .authorities(
                                                        new org.springframework.security.core
                                                                .authority.SimpleGrantedAuthority(
                                                                "ROLE_ACADEMIC_STAFF"),
                                                        new org.springframework.security.core
                                                                .authority.SimpleGrantedAuthority(
                                                                "SCOPE_ACADEMIC_STAFF|GLOBAL|*")))
                                .with(csrf())
                                .contentType("application/json")
                                .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/courses/" + id))
                .andExpect(jsonPath("$.code").value("CS101"));
    }

    @Test
    void missingCreditsAreRejectedBeforeCallingService() throws Exception {
        mvc.perform(
                        post("/api/v1/courses")
                                .with(user("staff").roles("ACADEMIC_STAFF"))
                                .with(csrf())
                                .contentType("application/json")
                                .content("{\"code\":\"CS101\",\"name\":\"Tin hoc\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void staffWriteWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(
                        post("/api/v1/courses")
                                .with(user("staff").roles("ACADEMIC_STAFF"))
                                .contentType("application/json")
                                .content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
