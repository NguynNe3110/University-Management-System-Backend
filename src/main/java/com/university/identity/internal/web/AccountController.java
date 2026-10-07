package com.university.identity.internal.web;

import com.university.identity.api.*;
import com.university.identity.internal.application.AccountService;
import com.university.shared.security.Access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@Profile("!course-demo")
@RequestMapping("/api/identity")
public class AccountController {
    private final AccountService service;
    private final IdentityDirectory directory;

    public AccountController(AccountService service, IdentityDirectory directory) {
        this.service = service;
        this.directory = directory;
    }

    @GetMapping("/me")
    public UserProfileView me() {
        return directory.findByUsername(Access.username()).orElseThrow();
    }

    @GetMapping("/me/access")
    public java.util.Map<String, Object> access() {
        return java.util.Map.of(
                "username",
                Access.username(),
                "authorities",
                Access.authentication().getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .toList());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/users")
    public ResponseEntity<UserProfileView> create(@Valid @RequestBody AccountRequest r) {
        var u = service.create(r);
        return ResponseEntity.created(URI.create("/api/identity/users/" + u.username())).body(u);
    }

    public record Status(@NotBlank String status) {}

    @PatchMapping("/users/{id}/status")
    public UserProfileView status(@PathVariable String id, @Valid @RequestBody Status r) {
        return service.status(id, r.status());
    }

    public record Grants(
            @NotNull @Size(min = 1, max = 20) @Valid List<AccountRequest.Grant> grants) {}

    @PutMapping("/users/{id}/grants")
    public ResponseEntity<Void> grants(@PathVariable String id, @Valid @RequestBody Grants r) {
        service.grants(id, r.grants());
        return ResponseEntity.noContent().build();
    }

    public record Password(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 12, max = 128) String newPassword) {
        @Override
        public String toString() {
            return "Password[redacted]";
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> password(@Valid @RequestBody Password r) {
        service.password(r.currentPassword(), r.newPassword());
        return ResponseEntity.noContent().build();
    }
}
