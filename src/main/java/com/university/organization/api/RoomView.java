package com.university.organization.api;

public record RoomView(
    String id,
    String code,
    String building,
    int capacity
) {}
