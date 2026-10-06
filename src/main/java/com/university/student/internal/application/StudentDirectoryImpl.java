package com.university.student.internal.application;

import com.university.student.api.StudentDirectory;
import com.university.student.api.StudentProfileView;
import com.university.student.internal.domain.StudentProfile;
import com.university.student.internal.persistence.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StudentDirectoryImpl implements StudentDirectory {

    private final StudentProfileRepository studentProfileRepository;

    public StudentDirectoryImpl(StudentProfileRepository studentProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
    }

    @Override
    public Optional<StudentProfileView> findByStudentCode(String studentCode) {
        return studentProfileRepository.findByStudentCode(studentCode).map(this::mapToView);
    }

    @Override
    public Optional<StudentProfileView> findById(String id) {
        return studentProfileRepository.findById(id).map(this::mapToView);
    }

    @Override
    public List<StudentProfileView> findAllStudents() {
        return studentProfileRepository.findAll().stream().map(this::mapToView).toList();
    }

    private StudentProfileView mapToView(StudentProfile s) {
        return new StudentProfileView(
            s.getId(),
            s.getStudentCode(),
            s.getFullName(),
            s.getEmail(),
            s.getProgramId(),
            s.getStatus()
        );
    }
}
