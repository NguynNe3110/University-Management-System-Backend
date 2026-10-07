package com.university.identity.internal.application;

import com.university.identity.internal.domain.AppUser;
import com.university.identity.internal.persistence.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Profile("!course-demo")
public class AdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String username, password, email;

    public AdminBootstrap(
            AppUserRepository users,
            JdbcTemplate jdbc,
            PasswordEncoder encoder,
            @Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email) {
        this.users = users;
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || users.count() != 0) return;
        if (password.length() < 12 || email.isBlank())
            throw new IllegalArgumentException(
                    "Bootstrap requires a password of 12+ characters and email");
        String id = UUID.randomUUID().toString();
        users.saveAndFlush(
                new AppUser(
                        id,
                        username,
                        encoder.encode(password),
                        "Technical administrator",
                        email,
                        "ADMIN",
                        "ACTIVE"));
        jdbc.update(
                "INSERT INTO"
                    + " account_grant(id,user_id,role,scope_type,scope_id,valid_from,valid_until)"
                    + " VALUES (?,?,'ADMIN','GLOBAL','*',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP +"
                    + " INTERVAL '1 year')",
                UUID.randomUUID().toString(),
                id);
    }
}
