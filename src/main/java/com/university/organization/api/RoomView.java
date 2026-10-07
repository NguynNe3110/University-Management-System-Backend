package com.university.organization.api;

public record RoomView(String id, String code, String building, int capacity, long version) {
    public RoomView(String id, String code, String building, int capacity) {
        this(id, code, building, capacity, 0);
    }
}
