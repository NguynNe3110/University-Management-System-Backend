package com.university.identity.api;

import java.util.List;
import java.util.Optional;

public interface IdentityDirectory {
    Optional<UserProfileView> findByUsername(String username);
    Optional<UserProfileView> findById(String id);
    List<UserProfileView> findAllUsers();
}
