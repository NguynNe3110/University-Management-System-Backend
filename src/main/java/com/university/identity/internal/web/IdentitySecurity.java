package com.university.identity.internal.web;

import com.university.identity.internal.persistence.AppUserRepository;

import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.ArrayList;

@Configuration(proxyBeanMethods = false)
@Profile("!course-demo")
public class IdentitySecurity {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService databaseUsers(AppUserRepository users, JdbcTemplate jdbc) {
        return username -> {
            var u =
                    users.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
            var authorities = new ArrayList<SimpleGrantedAuthority>();
            jdbc.query(
                    "SELECT role,scope_type,scope_id FROM account_grant WHERE user_id=? AND"
                            + " valid_from<=CURRENT_TIMESTAMP AND valid_until>CURRENT_TIMESTAMP",
                    (org.springframework.jdbc.core.RowCallbackHandler)
                            rs -> {
                                authorities.add(
                                        new SimpleGrantedAuthority("ROLE_" + rs.getString(1)));
                                authorities.add(
                                        new SimpleGrantedAuthority(
                                                "SCOPE_"
                                                        + rs.getString(1)
                                                        + "|"
                                                        + rs.getString(2)
                                                        + "|"
                                                        + rs.getString(3)));
                            },
                    u.getId());
            authorities.add(new SimpleGrantedAuthority("SELF_USER|" + u.getId()));
            if (u.getStudentId() != null)
                authorities.add(new SimpleGrantedAuthority("SELF_STUDENT|" + u.getStudentId()));
            if (u.getLecturerId() != null)
                authorities.add(new SimpleGrantedAuthority("SELF_LECTURER|" + u.getLecturerId()));
            return User.withUsername(u.getUsername())
                    .password(u.getPasswordHash())
                    .authorities(authorities)
                    .disabled(!"ACTIVE".equals(u.getStatus()))
                    .build();
        };
    }

    @Bean
    SecurityFilterChain identityFilterChain(HttpSecurity http) throws Exception {
        return http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(
                        r ->
                                r.requestMatchers("/actuator/health", "/actuator/health/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
