package com.university.teachingclass.internal.application;

import com.university.teachingclass.api.TeachingClassDirectory;
import com.university.teachingclass.api.TeachingClassView;
import com.university.teachingclass.internal.domain.TeachingClass;
import com.university.teachingclass.internal.persistence.TeachingClassRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TeachingClassDirectoryImpl implements TeachingClassDirectory {

    private final TeachingClassRepository teachingClassRepository;

    public TeachingClassDirectoryImpl(TeachingClassRepository teachingClassRepository) {
        this.teachingClassRepository = teachingClassRepository;
    }

    @Override
    public Optional<TeachingClassView> findClassById(String id) {
        return teachingClassRepository.findById(id).map(this::mapToView);
    }

    @Override
    public List<TeachingClassView> findClassesBySemester(String semesterId) {
        return teachingClassRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToView)
                .toList();
    }

    @Override
    public List<TeachingClassView> findAllClasses() {
        return teachingClassRepository.findAll().stream().map(this::mapToView).toList();
    }

    @Override
    public int getCapacity(String classId) {
        return teachingClassRepository
                .findById(classId)
                .map(TeachingClass::getMaxCapacity)
                .orElseThrow(
                        () ->
                                com.university.shared.exception.BusinessException.missing(
                                        "Teaching class"));
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public TeachingClassView lockForRegistration(String classId) {
        return teachingClassRepository
                .lockById(classId)
                .map(this::mapToView)
                .orElseThrow(
                        () ->
                                com.university.shared.exception.BusinessException.missing(
                                        "Teaching class"));
    }

    private TeachingClassView mapToView(TeachingClass c) {
        return new TeachingClassView(
                c.getId(),
                c.getCode(),
                c.getCourseId(),
                c.getSemesterId(),
                c.getRoomId(),
                c.getLecturerId(),
                c.getMaxCapacity(),
                c.getStatus(),
                c.getDepartmentId(),
                c.getTuitionRate(),
                c.getVersion());
    }
}
