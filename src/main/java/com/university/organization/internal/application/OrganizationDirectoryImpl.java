package com.university.organization.internal.application;

import com.university.organization.api.DepartmentView;
import com.university.organization.api.OrganizationDirectory;
import com.university.organization.api.RoomView;
import com.university.organization.internal.persistence.DepartmentRepository;
import com.university.organization.internal.persistence.RoomRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrganizationDirectoryImpl implements OrganizationDirectory {

    private final DepartmentRepository departmentRepository;
    private final RoomRepository roomRepository;

    public OrganizationDirectoryImpl(
            DepartmentRepository departmentRepository, RoomRepository roomRepository) {
        this.departmentRepository = departmentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Optional<DepartmentView> findDepartmentById(String id) {
        return departmentRepository
                .findById(id)
                .map(d -> new DepartmentView(d.getId(), d.getCode(), d.getName(), d.getVersion()));
    }

    @Override
    public Optional<RoomView> findRoomById(String id) {
        return roomRepository
                .findById(id)
                .map(
                        r ->
                                new RoomView(
                                        r.getId(),
                                        r.getCode(),
                                        r.getBuilding(),
                                        r.getCapacity(),
                                        r.getVersion()));
    }

    @Override
    public List<DepartmentView> findAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentView(d.getId(), d.getCode(), d.getName(), d.getVersion()))
                .toList();
    }

    @Override
    public List<RoomView> findAllRooms() {
        return roomRepository.findAll().stream()
                .map(
                        r ->
                                new RoomView(
                                        r.getId(),
                                        r.getCode(),
                                        r.getBuilding(),
                                        r.getCapacity(),
                                        r.getVersion()))
                .toList();
    }
}
