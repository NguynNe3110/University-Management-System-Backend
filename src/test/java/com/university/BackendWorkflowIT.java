package com.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

/** Runs against a disposable PostgreSQL container, or an explicitly provided test database. */
@SpringBootTest(
        properties = {
            "payments.simulator-enabled=true",
            "BOOTSTRAP_ADMIN_USERNAME=test-admin",
            "BOOTSTRAP_ADMIN_PASSWORD=initial-test-password",
            "BOOTSTRAP_ADMIN_EMAIL=admin@test.invalid",
            "logging.level.root=WARN"
        })
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackendWorkflowIT {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    static PostgreSQLContainer<?> postgres;
    static final String PASSWORD = "initial-test-password";

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getProperty("test.database.url");
        if (url != null) {
            registry.add("spring.datasource.url", () -> url);
            registry.add(
                    "spring.datasource.username",
                    () -> System.getProperty("test.database.username", "postgres"));
            registry.add(
                    "spring.datasource.password",
                    () -> System.getProperty("test.database.password", ""));
        } else {
            postgres = new PostgreSQLContainer<>("postgres:17-alpine");
            postgres.start();
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
    }

    private JsonNode call(
            MockHttpServletRequestBuilder request, String user, Object body, int expected)
            throws Exception {
        if (user != null) request.with(httpBasic(user, PASSWORD));
        if (body != null)
            request.contentType("application/json").content(json.writeValueAsString(body));
        addRealCsrf(request, user);
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus())
                .withFailMessage(
                        "HTTP %s: %s",
                        result.getResponse().getStatus(), result.getResponse().getContentAsString())
                .isEqualTo(expected);
        String text = result.getResponse().getContentAsString();
        return text.isBlank() ? json.createObjectNode() : json.readTree(text);
    }

    private void addRealCsrf(MockHttpServletRequestBuilder request, String user) throws Exception {
        if (user == null) return;
        var result =
                mvc.perform(get("/api/identity/csrf").with(httpBasic(user, PASSWORD))).andReturn();
        if (result.getResponse().getStatus() != 200) return;
        var token = json.readTree(result.getResponse().getContentAsString());
        request.cookie(result.getResponse().getCookies())
                .header(token.get("headerName").asText(), token.get("token").asText());
    }

    private String account(
            String username,
            String role,
            String type,
            String scope,
            String student,
            String lecturer)
            throws Exception {
        var body = new HashMap<String, Object>();
        body.put("username", username);
        body.put("password", PASSWORD);
        body.put("fullName", username);
        body.put("email", username + "@test.invalid");
        if (student != null) body.put("studentId", student);
        if (lecturer != null) body.put("lecturerId", lecturer);
        body.put(
                "grants",
                List.of(
                        Map.of(
                                "role",
                                role,
                                "scopeType",
                                type,
                                "scopeId",
                                scope,
                                "validFrom",
                                Instant.now().minusSeconds(60).toString(),
                                "validUntil",
                                Instant.now().plusSeconds(86400).toString())));
        return call(post("/api/identity/users"), "test-admin", body, 201).get("id").asText();
    }

    record Fixture(
            String department,
            String program,
            String semester,
            String lecturer,
            String student,
            String student2,
            String course,
            String teachingClass,
            String window) {}

    private Fixture fixture(String tag, int capacity) throws Exception {
        account(tag + "-academic", "ACADEMIC_STAFF", "GLOBAL", "*", null, null);
        account(tag + "-room", "ROOM_MANAGER", "GLOBAL", "*", null, null);
        var dep =
                call(
                                post("/api/organization/departments"),
                                tag + "-academic",
                                Map.of("code", tag, "name", tag),
                                201)
                        .get("id")
                        .asText();
        var program =
                call(
                                post("/api/academic/programs"),
                                tag + "-academic",
                                Map.of("code", tag, "name", tag, "departmentId", dep),
                                201)
                        .get("id")
                        .asText();
        var semester =
                call(
                                post("/api/academic/semesters"),
                                tag + "-academic",
                                Map.of("code", tag, "academicYear", "2026-2027", "term", 1),
                                201)
                        .get("id")
                        .asText();
        var lecturer =
                call(
                                post("/api/lecturers"),
                                tag + "-academic",
                                Map.of(
                                        "lecturerCode",
                                        tag,
                                        "fullName",
                                        tag,
                                        "email",
                                        tag + "-lecturer@test.invalid",
                                        "departmentId",
                                        dep,
                                        "status",
                                        "ACTIVE"),
                                201)
                        .get("id")
                        .asText();
        var student =
                call(
                                post("/api/students"),
                                tag + "-academic",
                                Map.of(
                                        "studentCode",
                                        tag + "1",
                                        "fullName",
                                        tag,
                                        "email",
                                        tag + "-s1@test.invalid",
                                        "programId",
                                        program,
                                        "status",
                                        "ACTIVE"),
                                201)
                        .get("id")
                        .asText();
        var student2 =
                call(
                                post("/api/students"),
                                tag + "-academic",
                                Map.of(
                                        "studentCode",
                                        tag + "2",
                                        "fullName",
                                        tag,
                                        "email",
                                        tag + "-s2@test.invalid",
                                        "programId",
                                        program,
                                        "status",
                                        "ACTIVE"),
                                201)
                        .get("id")
                        .asText();
        account(tag + "-student", "STUDENT", "GLOBAL", "*", student, null);
        account(tag + "-student2", "STUDENT", "GLOBAL", "*", student2, null);
        account(tag + "-lecturer", "LECTURER", "GLOBAL", "*", null, lecturer);
        account(tag + "-faculty", "FACULTY_STAFF", "DEPARTMENT", dep, null, null);
        account(tag + "-exam", "EXAM_STAFF", "DEPARTMENT", dep, null, null);
        account(tag + "-approver", "ACADEMIC_APPROVER", "DEPARTMENT", dep, null, null);
        account(tag + "-finance", "FINANCE_STAFF", "GLOBAL", "*", null, null);
        var course =
                call(
                                post("/api/v1/courses"),
                                tag + "-academic",
                                Map.of("code", tag, "name", tag, "credits", 3),
                                201)
                        .get("id")
                        .asText();
        call(
                put("/api/academic/programs/" + program + "/curriculum"),
                tag + "-academic",
                Map.of(
                        "version",
                        0,
                        "courses",
                        List.of(Map.of("courseId", course, "prerequisiteCourseIds", List.of()))),
                200);
        var teachingClass =
                call(
                                post("/api/teaching-classes"),
                                tag + "-academic",
                                Map.of(
                                        "code",
                                        tag,
                                        "courseId",
                                        course,
                                        "semesterId",
                                        semester,
                                        "departmentId",
                                        dep,
                                        "lecturerId",
                                        lecturer,
                                        "maxCapacity",
                                        capacity,
                                        "tuitionRate",
                                        1000000),
                                201)
                        .get("id")
                        .asText();
        call(
                post("/api/teaching-classes/" + teachingClass + "/open"),
                tag + "-academic",
                Map.of("version", 0),
                200);
        var window =
                call(
                                post("/api/enrollments/windows"),
                                tag + "-academic",
                                Map.of(
                                        "semesterId",
                                        semester,
                                        "programId",
                                        program,
                                        "opensAt",
                                        Instant.now().minusSeconds(3600).toString(),
                                        "closesAt",
                                        Instant.now().plusSeconds(3600).toString(),
                                        "cancellationDeadline",
                                        Instant.now().plusSeconds(3600).toString(),
                                        "maxCredits",
                                        20,
                                        "prerequisiteCourseIds",
                                        List.of(),
                                        "minimumPassingScore",
                                        5),
                                201)
                        .get("id")
                        .asText();
        return new Fixture(
                dep, program, semester, lecturer, student, student2, course, teachingClass, window);
    }

    private String session(Fixture f, String tag, Instant start, Instant end, int number)
            throws Exception {
        var room =
                call(
                                post("/api/organization/rooms"),
                                tag + "-room",
                                Map.of("code", tag + number, "building", "A", "capacity", 50),
                                201)
                        .get("id")
                        .asText();
        var s =
                call(
                        post("/api/timetable/sessions"),
                        tag + "-academic",
                        Map.of(
                                "teachingClassId",
                                f.teachingClass,
                                "sessionNumber",
                                number,
                                "roomId",
                                room,
                                "sessionDate",
                                start.atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                                        .toLocalDate()
                                        .toString(),
                                "startPeriod",
                                1,
                                "endPeriod",
                                3,
                                "startsAt",
                                start.toString(),
                                "endsAt",
                                end.toString()),
                        201);
        call(
                post("/api/timetable/sessions/" + s.get("id").asText() + "/publish"),
                tag + "-approver",
                Map.of("version", s.get("version").asLong()),
                200);
        return s.get("id").asText();
    }

    @Test
    void fullSemesterFlowWithObjectAuthorizationCsrfAndIdempotentPayments() throws Exception {
        String tag = "FLOW";
        var f = fixture(tag, 2);
        var enrollment =
                call(
                        post("/api/enrollments"),
                        tag + "-student",
                        Map.of(
                                "studentId",
                                f.student,
                                "teachingClassId",
                                f.teachingClass,
                                "windowId",
                                f.window),
                        201);
        call(
                post("/api/enrollments"),
                tag + "-student2",
                Map.of(
                        "studentId",
                        f.student,
                        "teachingClassId",
                        f.teachingClass,
                        "windowId",
                        f.window),
                403);
        call(
                get("/api/enrollments/" + enrollment.get("id").asText()),
                tag + "-student2",
                null,
                403);
        call(get("/api/identity/users"), tag + "-student", null, 403);
        var missingCsrf =
                mvc.perform(
                                post("/api/tuition/issue")
                                        .with(httpBasic(tag + "-finance", PASSWORD))
                                        .contentType("application/json")
                                        .content("{}"))
                        .andReturn();
        assertThat(missingCsrf.getResponse().getStatus()).isEqualTo(403);
        var tokenResponse =
                mvc.perform(get("/api/identity/csrf").with(httpBasic(tag + "-student", PASSWORD)))
                        .andReturn();
        assertThat(tokenResponse.getResponse().getStatus()).isEqualTo(200);
        var token = json.readTree(tokenResponse.getResponse().getContentAsString());
        var realCsrf =
                mvc.perform(
                                put("/api/identity/me/password")
                                        .with(httpBasic(tag + "-student", PASSWORD))
                                        .cookie(tokenResponse.getResponse().getCookies())
                                        .header(
                                                token.get("headerName").asText(),
                                                token.get("token").asText())
                                        .contentType("application/json")
                                        .content(
                                                json.writeValueAsString(
                                                        Map.of(
                                                                "currentPassword",
                                                                PASSWORD,
                                                                "newPassword",
                                                                PASSWORD))))
                        .andReturn();
        assertThat(realCsrf.getResponse().getStatus()).isEqualTo(204);

        String current =
                session(f, tag, Instant.now().minusSeconds(120), Instant.now().plusSeconds(600), 1);
        var attendance =
                call(
                        post("/api/attendance/sessions"),
                        tag + "-lecturer",
                        Map.of(
                                "timetableSessionId",
                                current,
                                "expiresAt",
                                Instant.now().plusSeconds(120).toString()),
                        201);
        String attendanceId = attendance.get("id").asText();
        var qr = Map.of("qrToken", attendance.get("qrToken").asText());
        var checked =
                call(
                        post("/api/attendance/sessions/" + attendanceId + "/check-in"),
                        tag + "-student",
                        qr,
                        200);
        assertThat(
                        call(
                                        post(
                                                "/api/attendance/sessions/"
                                                        + attendanceId
                                                        + "/check-in"),
                                        tag + "-student",
                                        qr,
                                        200)
                                .get("id"))
                .isEqualTo(checked.get("id"));
        call(
                post("/api/attendance/sessions/" + attendanceId + "/check-in"),
                tag + "-student2",
                qr,
                403);
        call(
                post("/api/attendance/sessions/" + attendanceId + "/close"),
                tag + "-lecturer",
                null,
                200);
        String ended =
                session(
                        f,
                        tag,
                        Instant.now().minusSeconds(7200),
                        Instant.now().minusSeconds(3600),
                        2);
        var held =
                call(
                        post("/api/timetable/sessions/" + ended + "/held"),
                        tag + "-lecturer",
                        Map.of("version", 1),
                        200);
        assertThat(held.get("status").asText()).isEqualTo("HELD");
        var log =
                call(
                        post("/api/teaching/logs"),
                        tag + "-lecturer",
                        Map.of(
                                "timetableSessionId",
                                ended,
                                "actualHours",
                                3,
                                "contentSummary",
                                "Delivered lesson"),
                        200);
        call(
                post("/api/teaching/logs/" + log.get("id").asText() + "/decision"),
                tag + "-faculty",
                Map.of("approve", true, "reason", "Verified"),
                200);

        var fee =
                call(
                        post("/api/tuition/issue"),
                        tag + "-finance",
                        Map.of("studentId", f.student, "semesterId", f.semester),
                        200);
        var feeAgain =
                call(
                        post("/api/tuition/issue"),
                        tag + "-finance",
                        Map.of("studentId", f.student, "semesterId", f.semester),
                        200);
        assertThat(feeAgain.get("amountDue").decimalValue()).isEqualByComparingTo("1000000");
        var payment =
                call(
                        post("/api/tuition/payments"),
                        tag + "-student",
                        Map.of(
                                "tuitionFeeId",
                                fee.get("id").asText(),
                                "requestKey",
                                "request-1",
                                "expiresAt",
                                Instant.now().plusSeconds(300).toString()),
                        200);
        String pid = payment.get("id").asText();
        var simulated = Map.of("transactionId", "FLOW-TXN", "amount", 1000000, "currency", "VND");
        call(post("/api/tuition/payments/" + pid + "/simulate"), tag + "-student", simulated, 200);
        call(post("/api/tuition/payments/" + pid + "/simulate"), tag + "-student", simulated, 200);
        assertThat(
                        call(
                                        get("/api/tuition/summary")
                                                .param("studentId", f.student)
                                                .param("semesterId", f.semester),
                                        tag + "-student",
                                        null,
                                        200)
                                .get("amountPaid")
                                .decimalValue())
                .isEqualByComparingTo("1000000");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM payment_transaction WHERE"
                                        + " transaction_id='FLOW-TXN'",
                                Integer.class))
                .isEqualTo(1);

        var draft =
                call(
                        put("/api/grades/classes/" + f.teachingClass),
                        tag + "-lecturer",
                        Map.of(
                                "attendanceWeight",
                                0.1,
                                "midtermWeight",
                                0.3,
                                "finalWeight",
                                0.6,
                                "version",
                                0,
                                "grades",
                                List.of(
                                        Map.of(
                                                "studentId",
                                                f.student,
                                                "attendanceScore",
                                                10,
                                                "midtermScore",
                                                8,
                                                "finalScore",
                                                9))),
                        200);
        assertThat(
                        call(
                                        get("/api/grades/by-student/" + f.student),
                                        tag + "-student",
                                        null,
                                        200)
                                .size())
                .isZero();
        var submitted =
                call(
                        post("/api/grades/classes/" + f.teachingClass + "/submit"),
                        tag + "-lecturer",
                        Map.of("version", draft.get("version").asLong()),
                        200);
        var lecturerAccount =
                call(get("/api/identity/users/" + tag + "-lecturer"), "test-admin", null, 200);
        var from = Instant.now().minusSeconds(60).toString();
        var until = Instant.now().plusSeconds(3600).toString();
        call(
                put("/api/identity/users/" + lecturerAccount.get("id").asText() + "/grants"),
                "test-admin",
                Map.of(
                        "grants",
                        List.of(
                                Map.of(
                                        "role",
                                        "LECTURER",
                                        "scopeType",
                                        "GLOBAL",
                                        "scopeId",
                                        "*",
                                        "validFrom",
                                        from,
                                        "validUntil",
                                        until),
                                Map.of(
                                        "role",
                                        "EXAM_STAFF",
                                        "scopeType",
                                        "DEPARTMENT",
                                        "scopeId",
                                        f.department,
                                        "validFrom",
                                        from,
                                        "validUntil",
                                        until))),
                204);
        call(get("/api/grades/classes/" + f.teachingClass), tag + "-exam", null, 200);
        call(
                post("/api/grades/classes/" + f.teachingClass + "/publish"),
                tag + "-lecturer",
                Map.of("version", submitted.get("version").asLong()),
                409);
        call(
                post("/api/grades/classes/" + f.teachingClass + "/publish"),
                tag + "-exam",
                Map.of("version", submitted.get("version").asLong()),
                200);
        var results = call(get("/api/grades/by-student/" + f.student), tag + "-student", null, 200);
        assertThat(results.get(0).get("totalScore").asDouble()).isEqualTo(8.8);
        var correction =
                call(
                        post("/api/grades/classes/" + f.teachingClass + "/corrections"),
                        tag + "-lecturer",
                        Map.of(
                                "scores",
                                Map.of(
                                        "studentId",
                                        f.student,
                                        "attendanceScore",
                                        10,
                                        "midtermScore",
                                        9,
                                        "finalScore",
                                        9),
                                "reason",
                                "Verified exam paper"),
                        200);
        call(
                get("/api/grades/corrections/" + correction.get("id").asText()),
                tag + "-exam",
                null,
                200);
        call(
                post("/api/grades/corrections/" + correction.get("id").asText() + "/decision"),
                tag + "-lecturer",
                Map.of("approve", true, "reason", "Self approval blocked"),
                409);
        call(
                post("/api/grades/corrections/" + correction.get("id").asText() + "/decision"),
                tag + "-exam",
                Map.of("approve", true, "reason", "Independent review"),
                200);
        var revised = call(get("/api/grades/by-student/" + f.student), tag + "-student", null, 200);
        assertThat(revised.get(0).get("totalScore").asDouble()).isEqualTo(9.1);
        assertThat(revised.get(0).get("resultRevision").asLong()).isEqualTo(1);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM grade_result_history WHERE correction_id=?",
                                Integer.class,
                                correction.get("id").asText()))
                .isEqualTo(1);
        var attendanceCorrection =
                call(
                        post("/api/attendance/sessions/" + attendanceId + "/corrections"),
                        tag + "-student",
                        Map.of(
                                "studentId",
                                f.student,
                                "requestedStatus",
                                "EXCUSED",
                                "reason",
                                "Documented reason"),
                        200);
        call(
                post(
                        "/api/attendance/corrections/"
                                + attendanceCorrection.get("id").asText()
                                + "/decision"),
                tag + "-faculty",
                Map.of("approve", true, "reason", "Verified evidence"),
                200);
        assertThat(
                        call(
                                        get("/api/attendance/" + checked.get("id").asText()),
                                        tag + "-student",
                                        null,
                                        200)
                                .get("status")
                                .asText())
                .isEqualTo("EXCUSED");
        account(tag + "-finance-approver", "FINANCE_APPROVER", "GLOBAL", "*", null, null);
        var adjustment =
                call(
                        post("/api/tuition/fees/" + fee.get("id").asText() + "/adjustments"),
                        tag + "-finance",
                        Map.of("delta", -100000, "reason", "Approved policy adjustment"),
                        200);
        call(
                post("/api/tuition/adjustments/" + adjustment.get("id").asText() + "/decision"),
                tag + "-finance-approver",
                Map.of("approve", true, "reason", "Verified adjustment"),
                200);
        var overpaid =
                call(
                        get("/api/tuition/summary")
                                .param("studentId", f.student)
                                .param("semesterId", f.semester),
                        tag + "-student",
                        null,
                        200);
        assertThat(overpaid.get("status").asText()).isEqualTo("OVERPAID");
        assertThat(overpaid.get("amountPaid").decimalValue()).isEqualByComparingTo("1000000");
        var me = call(get("/api/identity/me"), tag + "-student", null, 200);
        assertThat(
                        call(
                                        get(
                                                "/api/notifications/by-recipient/"
                                                        + me.get("id").asText()),
                                        tag + "-student",
                                        null,
                                        200)
                                .size())
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    void scopedExpiredAndLockedAccountsCannotUseAnotherPersonsData() throws Exception {
        String tag = "AUTH";
        var f = fixture(tag, 2);
        var otherDep =
                call(
                                post("/api/organization/departments"),
                                tag + "-academic",
                                Map.of("code", "AUTH-OTHER", "name", "Other department"),
                                201)
                        .get("id")
                        .asText();
        account("AUTH-scoped", "ACADEMIC_STAFF", "DEPARTMENT", otherDep, null, null);
        call(get("/api/students/AUTH1"), "AUTH-scoped", null, 403);
        assertThat(call(get("/api/students"), "AUTH-scoped", null, 200).size()).isZero();
        call(
                post("/api/v1/courses"),
                "AUTH-scoped",
                Map.of("code", "FORBIDDEN", "name", "Forbidden", "credits", 3),
                403);
        String userId =
                call(get("/api/identity/me"), tag + "-student", null, 200).get("id").asText();
        jdbc.update(
                "UPDATE account_grant SET valid_from=CURRENT_TIMESTAMP-INTERVAL '2"
                        + " hours',valid_until=CURRENT_TIMESTAMP-INTERVAL '1 hour' WHERE user_id=?",
                userId);
        call(get("/api/students/AUTH1"), tag + "-student", null, 403);
        call(
                patch("/api/identity/users/" + userId + "/status"),
                "test-admin",
                Map.of("status", "LOCKED"),
                200);
        call(get("/api/identity/me"), tag + "-student", null, 401);
    }

    @Test
    void fullTargetTransferRollsBackThenSuccessfulTransferDoesNotChargeAgain() throws Exception {
        String tag = "MOVE";
        var f = fixture(tag, 2);
        var old =
                call(
                        post("/api/enrollments"),
                        tag + "-student",
                        Map.of(
                                "studentId",
                                f.student,
                                "teachingClassId",
                                f.teachingClass,
                                "windowId",
                                f.window),
                        201);
        call(
                post("/api/tuition/issue"),
                tag + "-finance",
                Map.of("studentId", f.student, "semesterId", f.semester),
                200);
        var target =
                call(
                                post("/api/teaching-classes"),
                                tag + "-academic",
                                Map.of(
                                        "code",
                                        "MOVE-TARGET",
                                        "courseId",
                                        f.course,
                                        "semesterId",
                                        f.semester,
                                        "departmentId",
                                        f.department,
                                        "lecturerId",
                                        f.lecturer,
                                        "maxCapacity",
                                        1,
                                        "tuitionRate",
                                        1000000),
                                201)
                        .get("id")
                        .asText();
        call(
                post("/api/teaching-classes/" + target + "/open"),
                tag + "-academic",
                Map.of("version", 0),
                200);
        var occupant =
                call(
                        post("/api/enrollments"),
                        tag + "-student2",
                        Map.of(
                                "studentId",
                                f.student2,
                                "teachingClassId",
                                target,
                                "windowId",
                                f.window),
                        201);
        call(
                post("/api/enrollments/" + old.get("id").asText() + "/transfer"),
                tag + "-student",
                Map.of("teachingClassId", target, "windowId", f.window),
                409);
        assertThat(
                        call(
                                        get("/api/enrollments/" + old.get("id").asText()),
                                        tag + "-student",
                                        null,
                                        200)
                                .get("status")
                                .asText())
                .isEqualTo("ENROLLED");
        call(
                delete("/api/enrollments/" + occupant.get("id").asText()),
                tag + "-student2",
                null,
                200);
        call(
                post("/api/enrollments/" + old.get("id").asText() + "/transfer"),
                tag + "-student",
                Map.of("teachingClassId", target, "windowId", f.window),
                200);
        assertThat(
                        call(
                                        get("/api/enrollments/" + old.get("id").asText()),
                                        tag + "-student",
                                        null,
                                        200)
                                .get("status")
                                .asText())
                .isEqualTo("CANCELLED");
        var fee =
                call(
                        post("/api/tuition/issue"),
                        tag + "-finance",
                        Map.of("studentId", f.student, "semesterId", f.semester),
                        200);
        assertThat(fee.get("amountDue").decimalValue()).isEqualByComparingTo("1000000");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM tuition_line WHERE tuition_fee_id=?",
                                Integer.class,
                                fee.get("id").asText()))
                .isEqualTo(1);
    }

    @Test
    void mismatchedAndExpiredSimulatedTransactionsAreRecordedWithoutAllocation() throws Exception {
        String tag = "PAY";
        var f = fixture(tag, 1);
        call(
                post("/api/enrollments"),
                tag + "-student",
                Map.of(
                        "studentId",
                        f.student,
                        "teachingClassId",
                        f.teachingClass,
                        "windowId",
                        f.window),
                201);
        var fee =
                call(
                        post("/api/tuition/issue"),
                        tag + "-finance",
                        Map.of("studentId", f.student, "semesterId", f.semester),
                        200);
        var p =
                call(
                        post("/api/tuition/payments"),
                        tag + "-student",
                        Map.of(
                                "tuitionFeeId",
                                fee.get("id").asText(),
                                "requestKey",
                                "bad-amount",
                                "expiresAt",
                                Instant.now().plusSeconds(300).toString()),
                        200);
        var mismatch =
                call(
                        post("/api/tuition/payments/" + p.get("id").asText() + "/simulate"),
                        tag + "-student",
                        Map.of("transactionId", "PAY-MISMATCH", "amount", 100, "currency", "VND"),
                        200);
        assertThat(mismatch.get("status").asText()).isEqualTo("RECONCILIATION");
        var late =
                call(
                        post("/api/tuition/payments"),
                        tag + "-student",
                        Map.of(
                                "tuitionFeeId",
                                fee.get("id").asText(),
                                "requestKey",
                                "expired",
                                "expiresAt",
                                Instant.now().plusSeconds(300).toString()),
                        200);
        jdbc.update(
                "UPDATE payment_request SET expires_at=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE"
                        + " id=?",
                late.get("id").asText());
        call(
                post("/api/tuition/payments/" + late.get("id").asText() + "/simulate"),
                tag + "-student",
                Map.of("transactionId", "PAY-LATE", "amount", 1000000, "currency", "VND"),
                200);
        assertThat(
                        call(
                                        get("/api/tuition/summary")
                                                .param("studentId", f.student)
                                                .param("semesterId", f.semester),
                                        tag + "-student",
                                        null,
                                        200)
                                .get("amountPaid")
                                .decimalValue())
                .isEqualByComparingTo("0");
        assertThat(call(get("/api/tuition/reconciliation"), tag + "-finance", null, 200).size())
                .isGreaterThanOrEqualTo(2);
        call(
                post("/api/tuition/payments/" + late.get("id").asText() + "/simulate"),
                tag + "-student",
                Map.of("transactionId", "PAY-LATE", "amount", 5, "currency", "VND"),
                409);
    }

    @Test
    void scheduleChangeApprovalChecksConflictsAndKeepsPublishedHistory() throws Exception {
        String tag = "SCHEDULE";
        var f = fixture(tag, 2);
        var start = Instant.now().plusSeconds(3600);
        var end = start.plusSeconds(600);
        String first = session(f, tag, start, end, 1);
        String second = session(f, tag, start.plusSeconds(3600), end.plusSeconds(3600), 2);
        var other = call(get("/api/timetable/sessions/" + second), tag + "-academic", null, 200);
        var conflict =
                call(
                        post("/api/timetable/sessions/" + first + "/changes"),
                        tag + "-lecturer",
                        Map.of(
                                "cancel",
                                false,
                                "roomId",
                                other.get("roomId").asText(),
                                "startsAt",
                                other.get("startsAt").asText(),
                                "endsAt",
                                other.get("endsAt").asText(),
                                "reason",
                                "Change requested"),
                        200);
        call(
                post("/api/timetable/changes/" + conflict.get("id").asText() + "/decision"),
                tag + "-approver",
                Map.of("approve", true, "reason", "Review conflict"),
                409);
        var cancel =
                call(
                        post("/api/timetable/sessions/" + first + "/changes"),
                        tag + "-lecturer",
                        Map.of("cancel", true, "reason", "Approved cancellation reason"),
                        200);
        call(
                post("/api/timetable/changes/" + cancel.get("id").asText() + "/decision"),
                tag + "-approver",
                Map.of("approve", true, "reason", "Verified cancellation"),
                200);
        assertThat(
                        call(get("/api/timetable/sessions/" + first), tag + "-academic", null, 200)
                                .get("status")
                                .asText())
                .isEqualTo("CANCELLED");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM timetable_history WHERE"
                                        + " timetable_session_id=?",
                                Integer.class,
                                first))
                .isEqualTo(1);
    }

    @Test
    void concurrentStudentsCompeteForOneSeatAndFailedTransferPreservesOriginal() throws Exception {
        String tag = "RACE";
        var f = fixture(tag, 1);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                final int n = i;
                futures.add(
                        executor.submit(
                                () -> {
                                    ready.countDown();
                                    start.await(10, TimeUnit.SECONDS);
                                    var request =
                                            post("/api/enrollments")
                                                    .with(
                                                            httpBasic(
                                                                    tag
                                                                            + (n == 0
                                                                                    ? "-student"
                                                                                    : "-student2"),
                                                                    PASSWORD))
                                                    .contentType("application/json")
                                                    .content(
                                                            json.writeValueAsString(
                                                                    Map.of(
                                                                            "studentId",
                                                                            n == 0
                                                                                    ? f.student
                                                                                    : f.student2,
                                                                            "teachingClassId",
                                                                            f.teachingClass,
                                                                            "windowId",
                                                                            f.window)));
                                    addRealCsrf(request, tag + (n == 0 ? "-student" : "-student2"));
                                    return mvc.perform(request)
                                            .andReturn()
                                            .getResponse()
                                            .getStatus();
                                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            var statuses = new ArrayList<Integer>();
            for (var future : futures) statuses.add(future.get(30, TimeUnit.SECONDS));
            assertThat(statuses).containsExactlyInAnyOrder(201, 409);
        }
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM student_enrollment WHERE teaching_class_id=?"
                                        + " AND status='ENROLLED'",
                                Integer.class,
                                f.teachingClass))
                .isEqualTo(1);
        var active =
                jdbc.queryForMap(
                        "SELECT id,student_id FROM student_enrollment WHERE teaching_class_id=? AND"
                                + " status='ENROLLED'",
                        f.teachingClass);
        String winner =
                active.get("student_id").equals(f.student) ? tag + "-student" : tag + "-student2";
        String id = active.get("id").toString();
        call(
                post("/api/enrollments/" + id + "/transfer"),
                winner,
                Map.of("teachingClassId", UUID.randomUUID().toString(), "windowId", f.window),
                404);
        assertThat(call(get("/api/enrollments/" + id), winner, null, 200).get("status").asText())
                .isEqualTo("ENROLLED");
        call(delete("/api/enrollments/" + id), winner, null, 200);
        call(
                post("/api/enrollments"),
                winner,
                Map.of(
                        "studentId",
                        active.get("student_id"),
                        "teachingClassId",
                        f.teachingClass,
                        "windowId",
                        f.window),
                201);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM student_enrollment WHERE teaching_class_id=?",
                                Integer.class,
                                f.teachingClass))
                .isEqualTo(1);
    }

    @Test
    void academicStructureCurriculumPrerequisitesAndRoomAvailabilityAreEnforced() throws Exception {
        String tag = "FOUND";
        var f = fixture(tag, 2);
        var cohort =
                call(
                        post("/api/academic/cohorts"),
                        tag + "-academic",
                        Map.of("code", tag, "admissionYear", 2026),
                        200);
        var administrative =
                call(
                        post("/api/academic/administrative-classes"),
                        tag + "-academic",
                        Map.of(
                                "code",
                                tag,
                                "programId",
                                f.program,
                                "cohortId",
                                cohort.get("id").asText()),
                        200);
        var linked =
                call(
                        put("/api/students/" + f.student),
                        tag + "-academic",
                        Map.of(
                                "studentCode",
                                tag + "1",
                                "fullName",
                                tag,
                                "email",
                                tag + "-s1@test.invalid",
                                "programId",
                                f.program,
                                "status",
                                "ACTIVE",
                                "administrativeClassId",
                                administrative.get("id").asText(),
                                "version",
                                0),
                        200);
        assertThat(linked.get("administrativeClassId")).isEqualTo(administrative.get("id"));
        call(
                put("/api/academic/programs/" + f.program + "/curriculum"),
                tag + "-academic",
                Map.of(
                        "version",
                        1,
                        "courses",
                        List.of(
                                Map.of(
                                        "courseId",
                                        f.course,
                                        "prerequisiteCourseIds",
                                        List.of(f.course)))),
                400);
        call(
                put("/api/academic/programs/" + f.program + "/curriculum"),
                tag + "-academic",
                Map.of(
                        "version",
                        0,
                        "courses",
                        List.of(Map.of("courseId", f.course, "prerequisiteCourseIds", List.of()))),
                409);
        String next =
                call(
                                post("/api/v1/courses"),
                                tag + "-academic",
                                Map.of("code", "FOUNDNEXT", "name", "Next course", "credits", 3),
                                201)
                        .get("id")
                        .asText();
        var target =
                call(
                                post("/api/teaching-classes"),
                                tag + "-academic",
                                Map.of(
                                        "code",
                                        "FOUND-NEXT",
                                        "courseId",
                                        next,
                                        "semesterId",
                                        f.semester,
                                        "departmentId",
                                        f.department,
                                        "lecturerId",
                                        f.lecturer,
                                        "maxCapacity",
                                        2,
                                        "tuitionRate",
                                        1000000),
                                201)
                        .get("id")
                        .asText();
        call(
                post("/api/teaching-classes/" + target + "/open"),
                tag + "-academic",
                Map.of("version", 0),
                200);
        call(
                post("/api/enrollments"),
                tag + "-student",
                Map.of("studentId", f.student, "teachingClassId", target, "windowId", f.window),
                409);
        call(
                put("/api/academic/programs/" + f.program + "/curriculum"),
                tag + "-academic",
                Map.of(
                        "version",
                        1,
                        "courses",
                        List.of(
                                Map.of("courseId", f.course, "prerequisiteCourseIds", List.of()),
                                Map.of(
                                        "courseId",
                                        next,
                                        "prerequisiteCourseIds",
                                        List.of(f.course)))),
                200);
        assertThat(
                        call(
                                        get("/api/academic/programs/" + f.program + "/curriculum")
                                                .param("version", "1"),
                                        tag + "-academic",
                                        null,
                                        200)
                                .get("courses")
                                .size())
                .isEqualTo(1);
        call(
                post("/api/enrollments"),
                tag + "-student",
                Map.of("studentId", f.student, "teachingClassId", target, "windowId", f.window),
                409);
        var limits =
                call(
                        post("/api/enrollments/windows"),
                        tag + "-academic",
                        Map.of(
                                "semesterId",
                                f.semester,
                                "programId",
                                f.program,
                                "opensAt",
                                Instant.now().minusSeconds(60).toString(),
                                "closesAt",
                                Instant.now().plusSeconds(3600).toString(),
                                "cancellationDeadline",
                                Instant.now().plusSeconds(3600).toString(),
                                "maxCredits",
                                2,
                                "prerequisiteCourseIds",
                                List.of(),
                                "minimumPassingScore",
                                5),
                        201);
        call(
                post("/api/enrollments"),
                tag + "-student",
                Map.of(
                        "studentId",
                        f.student,
                        "teachingClassId",
                        f.teachingClass,
                        "windowId",
                        limits.get("id").asText()),
                409);
        var enrollment =
                call(
                        post("/api/enrollments"),
                        tag + "-student",
                        Map.of(
                                "studentId",
                                f.student,
                                "teachingClassId",
                                f.teachingClass,
                                "windowId",
                                f.window),
                        201);
        jdbc.update(
                "UPDATE registration_window SET cancellation_deadline=CURRENT_TIMESTAMP-INTERVAL '1"
                    + " second' WHERE id=?",
                f.window);
        call(
                delete("/api/enrollments/" + enrollment.get("id").asText()),
                tag + "-student",
                null,
                409);
        String room =
                call(
                                post("/api/organization/rooms"),
                                tag + "-room",
                                Map.of("code", "FOUND-ROOM", "building", "A", "capacity", 50),
                                201)
                        .get("id")
                        .asText();
        var start = Instant.now().plusSeconds(3600);
        var end = start.plusSeconds(1800);
        var blocked =
                call(
                        post("/api/organization/rooms/" + room + "/blocks"),
                        tag + "-room",
                        Map.of(
                                "startsAt",
                                start.toString(),
                                "endsAt",
                                end.toString(),
                                "reason",
                                "Maintenance"),
                        200);
        var body =
                Map.of(
                        "teachingClassId",
                        f.teachingClass,
                        "sessionNumber",
                        1,
                        "roomId",
                        room,
                        "sessionDate",
                        start.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate().toString(),
                        "startPeriod",
                        1,
                        "endPeriod",
                        3,
                        "startsAt",
                        start.toString(),
                        "endsAt",
                        end.toString());
        call(post("/api/timetable/sessions"), tag + "-academic", body, 409);
        call(
                delete("/api/organization/rooms/" + room + "/blocks/" + blocked.get("id").asText()),
                tag + "-room",
                null,
                204);
        var session = call(post("/api/timetable/sessions"), tag + "-academic", body, 201);
        call(
                post("/api/timetable/sessions/" + session.get("id").asText() + "/publish"),
                tag + "-approver",
                Map.of("version", 0),
                200);
        call(
                post("/api/organization/rooms/" + room + "/blocks"),
                tag + "-room",
                Map.of(
                        "startsAt",
                        start.toString(),
                        "endsAt",
                        end.toString(),
                        "reason",
                        "Conflict"),
                409);
        account(tag + "-report", "REPORT_VIEWER", "GLOBAL", "*", null, null);
        var report = call(get("/api/reporting/overview"), tag + "-report", null, 200);
        assertThat(report.get("totalCourses").asLong()).isPositive();
        assertThat(report.get("totalClasses").asLong()).isPositive();
    }

    @AfterAll
    void stopContainer() {
        if (postgres != null) postgres.stop();
    }
}
