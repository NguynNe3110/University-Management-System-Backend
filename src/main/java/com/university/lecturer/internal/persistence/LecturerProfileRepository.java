package com.university.lecturer.internal.persistence;

import com.university.lecturer.internal.domain.LecturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, String> {
    Optional<LecturerProfile> findByLecturerCode(String lecturerCode);
}
