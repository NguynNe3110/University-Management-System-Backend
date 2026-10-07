package com.university.academic.internal.application;

import com.university.academic.api.*;
import com.university.course.api.CourseCatalog;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AcademicStructureService implements AcademicStructure {
    private final JdbcTemplate jdbc;
    private final AcademicCatalog academic;
    private final CourseCatalog courses;
    private final TechnicalAudit audit;

    public AcademicStructureService(
            JdbcTemplate jdbc,
            AcademicCatalog academic,
            CourseCatalog courses,
            TechnicalAudit audit) {
        this.jdbc = jdbc;
        this.academic = academic;
        this.courses = courses;
        this.audit = audit;
    }

    public CohortView createCohort(String code, int year) {
        Access.require("ACADEMIC_STAFF", "GLOBAL", "*");
        var id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO cohort(id,code,admission_year) VALUES (?,?,?)", id, code.trim(), year);
        audit.record("academic", id, "CREATE_COHORT", code);
        return new CohortView(id, code.trim(), year);
    }

    @Transactional(readOnly = true)
    public List<CohortView> cohorts() {
        return jdbc.query(
                "SELECT * FROM cohort ORDER BY admission_year,code",
                (rs, n) ->
                        new CohortView(
                                rs.getString("id"),
                                rs.getString("code"),
                                rs.getInt("admission_year")));
    }

    public AdministrativeClassView createClass(String code, String program, String cohort) {
        var p =
                academic.findProgramById(program)
                        .orElseThrow(() -> BusinessException.missing("Program"));
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", p.departmentId());
        if (jdbc.queryForObject("SELECT count(*) FROM cohort WHERE id=?", Integer.class, cohort)
                == 0) throw BusinessException.missing("Cohort");
        String id = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO administrative_class(id,code,program_id,cohort_id) VALUES (?,?,?,?)",
                id,
                code.trim(),
                program,
                cohort);
        audit.record("academic", id, "CREATE_ADMINISTRATIVE_CLASS", code);
        return findAdministrativeClass(id).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministrativeClassView> findAdministrativeClass(String id) {
        return jdbc
                .query(
                        "SELECT * FROM administrative_class WHERE id=?",
                        (rs, n) ->
                                new AdministrativeClassView(
                                        rs.getString("id"),
                                        rs.getString("code"),
                                        rs.getString("program_id"),
                                        rs.getString("cohort_id")),
                        id)
                .stream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<AdministrativeClassView> classes() {
        return jdbc.query(
                "SELECT * FROM administrative_class ORDER BY code",
                (rs, n) ->
                        new AdministrativeClassView(
                                rs.getString("id"),
                                rs.getString("code"),
                                rs.getString("program_id"),
                                rs.getString("cohort_id")));
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumView curriculum(String program) {
        if (academic.findProgramById(program).isEmpty()) throw BusinessException.missing("Program");
        var versions =
                jdbc.query(
                        "SELECT version FROM curriculum_config WHERE program_id=?",
                        (rs, n) -> rs.getLong(1),
                        program);
        var rows =
                jdbc.query(
                        "SELECT course_id,prerequisites FROM curriculum_course WHERE program_id=?"
                            + " ORDER BY course_id",
                        (rs, n) ->
                                new CurriculumView.Course(
                                        rs.getString(1),
                                        rs.getString(2).isBlank()
                                                ? List.of()
                                                : List.of(rs.getString(2).split(","))),
                        program);
        return new CurriculumView(program, versions.isEmpty() ? 0 : versions.getFirst(), rows);
    }

    public CurriculumView curriculum(String program, CurriculumRequest r) {
        var p =
                academic.findProgramById(program)
                        .orElseThrow(() -> BusinessException.missing("Program"));
        Access.require("ACADEMIC_STAFF", "DEPARTMENT", p.departmentId());
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "curriculum:" + program);
        if (curriculum(program).version() != r.version())
            throw BusinessException.conflict("Curriculum version changed");
        var graph = new HashMap<String, List<String>>();
        for (var row : r.courses()) {
            if (graph.putIfAbsent(row.courseId(), row.prerequisiteCourseIds()) != null)
                throw new IllegalArgumentException("Duplicate curriculum course");
            var c =
                    courses.findById(UUID.fromString(row.courseId()))
                            .orElseThrow(() -> BusinessException.missing("Course"));
            if (!c.active()) throw BusinessException.conflict("Course archived");
            for (var prerequisite : row.prerequisiteCourseIds())
                if (courses.findById(UUID.fromString(prerequisite)).isEmpty())
                    throw BusinessException.missing("Prerequisite course");
        }
        for (var id : graph.keySet()) acyclic(id, graph, new HashSet<>(), new HashSet<>());
        jdbc.update(
                "INSERT INTO"
                    + " curriculum_history(program_id,version,course_id,prerequisites,changed_by)"
                    + " SELECT program_id,?,course_id,prerequisites,? FROM curriculum_course WHERE"
                    + " program_id=?",
                r.version(),
                Access.username(),
                program);
        jdbc.update("DELETE FROM curriculum_course WHERE program_id=?", program);
        for (var row : r.courses())
            jdbc.update(
                    "INSERT INTO curriculum_course(program_id,course_id,prerequisites) VALUES"
                        + " (?,?,?)",
                    program,
                    row.courseId(),
                    String.join(",", row.prerequisiteCourseIds()));
        jdbc.update(
                "INSERT INTO curriculum_config(program_id,version) VALUES (?,1) ON"
                    + " CONFLICT(program_id) DO UPDATE SET version=curriculum_config.version+1",
                program);
        audit.record("academic", program, "REPLACE_CURRICULUM", r.toString());
        return curriculum(program);
    }

    private void acyclic(
            String id, Map<String, List<String>> graph, Set<String> visiting, Set<String> done) {
        if (done.contains(id)) return;
        if (!visiting.add(id))
            throw new IllegalArgumentException("Curriculum prerequisites contain a cycle");
        for (var next : graph.getOrDefault(id, List.of())) acyclic(next, graph, visiting, done);
        visiting.remove(id);
        done.add(id);
    }

    @Transactional(readOnly = true)
    public CurriculumView curriculumVersion(String program, long version) {
        if (version < 1) throw new IllegalArgumentException("Curriculum version must be positive");
        var current = curriculum(program);
        if (current.version() == version) return current;
        var rows =
                jdbc.query(
                        "SELECT course_id,prerequisites FROM curriculum_history WHERE program_id=?"
                            + " AND version=? ORDER BY course_id",
                        (rs, n) ->
                                new CurriculumView.Course(
                                        rs.getString(1),
                                        rs.getString(2).isBlank()
                                                ? List.of()
                                                : List.of(rs.getString(2).split(","))),
                        program,
                        version);
        if (rows.isEmpty()) throw BusinessException.missing("Curriculum version");
        return new CurriculumView(program, version, rows);
    }
}
