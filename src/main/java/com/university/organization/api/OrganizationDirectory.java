package com.university.organization.api;

import java.util.List;
import java.util.Optional;

public interface OrganizationDirectory {
    Optional<DepartmentView> findDepartmentById(String id);
    Optional<RoomView> findRoomById(String id);
    List<DepartmentView> findAllDepartments();
    List<RoomView> findAllRooms();
}
