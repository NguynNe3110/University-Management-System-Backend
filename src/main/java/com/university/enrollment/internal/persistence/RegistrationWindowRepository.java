package com.university.enrollment.internal.persistence;

import com.university.enrollment.internal.domain.RegistrationWindow;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationWindowRepository extends JpaRepository<RegistrationWindow, String> {}
