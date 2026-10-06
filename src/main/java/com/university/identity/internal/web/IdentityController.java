package com.university.identity.internal.web;

import com.university.identity.api.IdentityDirectory;
import com.university.identity.api.UserProfileView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityDirectory identityDirectory;

    public IdentityController(IdentityDirectory identityDirectory) {
        this.identityDirectory = identityDirectory;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileView>> getAllUsers() {
        return ResponseEntity.ok(identityDirectory.findAllUsers());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserProfileView> getUserByUsername(@PathVariable String username) {
        return identityDirectory.findByUsername(username)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
