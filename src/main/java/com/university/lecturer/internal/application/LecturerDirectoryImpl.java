package com.university.lecturer.internal.application;

import com.university.lecturer.api.LecturerDirectory;
import com.university.lecturer.api.LecturerProfileView;
import com.university.lecturer.internal.domain.LecturerProfile;
import com.university.lecturer.internal.persistence.LecturerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LecturerDirectoryImpl implements LecturerDirectory {

    private final LecturerProfileRepository lecturerProfileRepository;

    public LecturerDirectoryImpl(LecturerProfileRepository lecturerProfileRepository) {
        this.lecturerProfileRepository = lecturerProfileRepository;
    }

    @Override
    public Optional<LecturerProfileView> findByLecturerCode(String lecturerCode) {
        return lecturerProfileRepository.findByLecturerCode(lecturerCode).map(this::mapToView);
    }

    @Override
    public Optional<LecturerProfileView> findById(String id) {
        return lecturerProfileRepository.findById(id).map(this::mapToView);
    }

    @Override
    public List<LecturerProfileView> findAllLecturers() {
        return lecturerProfileRepository.findAll().stream().map(this::mapToView).toList();
    }

    private LecturerProfileView mapToView(LecturerProfile l) {
        return new LecturerProfileView(
            l.getId(),
            l.getLecturerCode(),
            l.getFullName(),
            l.getEmail(),
            l.getDepartmentId(),
            l.getStatus()
        );
    }
}
