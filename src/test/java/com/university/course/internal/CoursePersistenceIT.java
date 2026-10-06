package com.university.course.internal;

import com.university.course.api.CourseCatalog;
import com.university.course.internal.application.CourseService;
import com.university.course.internal.application.CourseFailure;
import com.university.course.internal.domain.Course;
import com.university.course.internal.persistence.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.password=testcontainer-only", "COURSE_DEMO_PASSWORD=test-demo-only"})
@AutoConfigureMockMvc
@ActiveProfiles("course-demo")
@Testcontainers
class CoursePersistenceIT {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired CourseService service;
    @Autowired CourseCatalog catalog;
    @Autowired CourseRepository repository;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void demoAuthenticationCsrfAndHttpWritesWorkAgainstPostgres() throws Exception {
        var csrfResult = mvc.perform(get("/api/v1/demo/csrf").with(httpBasic("academic.demo", "test-demo-only")))
                .andExpect(status().isOk()).andReturn();
        var csrf = json.readTree(csrfResult.getResponse().getContentAsString());
        var session = (MockHttpSession) csrfResult.getRequest().getSession(false);
        var created = mvc.perform(post("/api/v1/courses").with(httpBasic("academic.demo", "test-demo-only"))
                        .session(session).header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType("application/json")
                        .content("{\"code\":\"HTTP101\",\"name\":\"HTTP course\",\"credits\":3}"))
                .andExpect(status().isCreated()).andReturn();
        var response = json.readTree(created.getResponse().getContentAsString());
        mvc.perform(get("/api/v1/courses/" + response.get("id").asText())
                        .with(httpBasic("academic.demo", "test-demo-only")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("HTTP101"));
        assertThat(catalog.findById(java.util.UUID.fromString(response.get("id").asText()))).isPresent();
    }

    @Test
    void persistsUpdatesAndArchivesThroughThePublicReadFacade() {
        var created = service.create("  FLOW101 ", "Tin học", 3);
        var updated = service.update(created.id(), "Tin học cơ sở", 4, created.version());
        assertThat(updated.version()).isGreaterThan(created.version());
        assertThatThrownBy(() -> service.update(created.id(), "Ghi đè cũ", 3, created.version()))
                .isInstanceOf(CourseFailure.class).hasMessageContaining("changed");
        var archived = service.archive(created.id(), updated.version());
        assertThat(catalog.findById(created.id())).contains(archived);
        assertThat(archived.active()).isFalse();
        assertThatThrownBy(() -> service.update(created.id(), "Tên mới", 3, archived.version()))
                .isInstanceOf(CourseFailure.class).hasMessageContaining("Archived");
    }

    @Test
    void databaseEnforcesUniqueCodeEvenWithoutServicePrecheck() {
        service.create("UNIQUE101", "Học phần", 3);
        assertThatThrownBy(() -> repository.saveAndFlush(new Course("UNIQUE101", "Bản trùng", 4)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
