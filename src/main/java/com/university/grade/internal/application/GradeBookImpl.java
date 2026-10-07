package com.university.grade.internal.application;

import com.university.grade.api.GradeBook;
import com.university.grade.api.StudentGradeView;
import com.university.grade.internal.persistence.StudentGradeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GradeBookImpl implements GradeBook {

    private final StudentGradeRepository studentGradeRepository;

    public GradeBookImpl(StudentGradeRepository studentGradeRepository) {
        this.studentGradeRepository = studentGradeRepository;
    }

    @Override
    public Optional<StudentGradeView> findGrade(String studentId, String teachingClassId) {
        return studentGradeRepository
                .findByStudentIdAndTeachingClassId(studentId, teachingClassId)
                .map(
                        g ->
                                new StudentGradeView(
                                        g.getId(),
                                        g.getStudentId(),
                                        g.getTeachingClassId(),
                                        g.getAttendanceScore(),
                                        g.getMidtermScore(),
                                        g.getFinalScore(),
                                        g.getTotalScore(),
                                        g.getStatus(),
                                        g.getResultRevision()));
    }

    @Override
    public List<StudentGradeView> findGradesByStudent(String studentId) {
        return studentGradeRepository.findByStudentId(studentId).stream()
                .map(
                        g ->
                                new StudentGradeView(
                                        g.getId(),
                                        g.getStudentId(),
                                        g.getTeachingClassId(),
                                        g.getAttendanceScore(),
                                        g.getMidtermScore(),
                                        g.getFinalScore(),
                                        g.getTotalScore(),
                                        g.getStatus(),
                                        g.getResultRevision()))
                .toList();
    }
}
