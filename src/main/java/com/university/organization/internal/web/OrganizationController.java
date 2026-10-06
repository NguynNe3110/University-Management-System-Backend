package com.university.organization.internal.web;

import com.university.organization.api.DepartmentView;
import com.university.organization.api.OrganizationDirectory;
import com.university.organization.api.RoomView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationDirectory organizationDirectory;

    public OrganizationController(OrganizationDirectory organizationDirectory) {
        this.organizationDirectory = organizationDirectory;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentView>> getAllDepartments() {
        return ResponseEntity.ok(organizationDirectory.findAllDepartments());
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentView> getDepartmentById(@PathVariable String id) {
        return organizationDirectory.findDepartmentById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomView>> getAllRooms() {
        return ResponseEntity.ok(organizationDirectory.findAllRooms());
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomView> getRoomById(@PathVariable String id) {
        return organizationDirectory.findRoomById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
