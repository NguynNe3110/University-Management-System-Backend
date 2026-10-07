package com.university.identity.internal.persistence;

import com.university.identity.internal.domain.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByStudentId(String studentId);
}
