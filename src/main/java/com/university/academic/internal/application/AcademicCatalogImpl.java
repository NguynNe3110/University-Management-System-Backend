package com.university.academic.internal.application;

import com.university.academic.api.AcademicCatalog;
import com.university.academic.api.ProgramView;
import com.university.academic.api.SemesterView;
import com.university.academic.internal.persistence.ProgramRepository;
import com.university.academic.internal.persistence.SemesterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AcademicCatalogImpl implements AcademicCatalog {

    private final ProgramRepository programRepository;
    private final SemesterRepository semesterRepository;

    public AcademicCatalogImpl(
            ProgramRepository programRepository, SemesterRepository semesterRepository) {
        this.programRepository = programRepository;
        this.semesterRepository = semesterRepository;
    }

    @Override
    public Optional<ProgramView> findProgramById(String id) {
        return programRepository
                .findById(id)
                .map(
                        p ->
                                new ProgramView(
                                        p.getId(),
                                        p.getCode(),
                                        p.getName(),
                                        p.getDepartmentId(),
                                        p.getVersion()));
    }

    @Override
    public Optional<SemesterView> findSemesterById(String id) {
        return semesterRepository
                .findById(id)
                .map(
                        s ->
                                new SemesterView(
                                        s.getId(),
                                        s.getCode(),
                                        s.getAcademicYear(),
                                        s.getTerm(),
                                        s.getVersion()));
    }

    @Override
    public List<ProgramView> findAllPrograms() {
        return programRepository.findAll().stream()
                .map(
                        p ->
                                new ProgramView(
                                        p.getId(),
                                        p.getCode(),
                                        p.getName(),
                                        p.getDepartmentId(),
                                        p.getVersion()))
                .toList();
    }

    @Override
    public List<SemesterView> findAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(
                        s ->
                                new SemesterView(
                                        s.getId(),
                                        s.getCode(),
                                        s.getAcademicYear(),
                                        s.getTerm(),
                                        s.getVersion()))
                .toList();
    }
}
