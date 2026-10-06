package com.university.identity.api;

public record UserProfileView(
    String id,
    String username,
    String fullName,
    String email,
    String roles,
    String status
) {}
