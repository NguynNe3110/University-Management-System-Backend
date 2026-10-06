package com.university.identity.internal.application;

import com.university.identity.api.IdentityDirectory;
import com.university.identity.api.UserProfileView;
import com.university.identity.internal.domain.AppUser;
import com.university.identity.internal.persistence.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class IdentityDirectoryImpl implements IdentityDirectory {

    private final AppUserRepository appUserRepository;

    public IdentityDirectoryImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public Optional<UserProfileView> findByUsername(String username) {
        return appUserRepository.findByUsername(username).map(this::mapToView);
    }

    @Override
    public Optional<UserProfileView> findById(String id) {
        return appUserRepository.findById(id).map(this::mapToView);
    }

    @Override
    public List<UserProfileView> findAllUsers() {
        return appUserRepository.findAll().stream().map(this::mapToView).toList();
    }

    private UserProfileView mapToView(AppUser u) {
        return new UserProfileView(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRoles(), u.getStatus());
    }
}
