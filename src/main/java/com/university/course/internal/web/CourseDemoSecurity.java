package com.university.course.internal.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Temporary manual demo adapter. Replaced by identity when authentication is implemented. */
@Configuration(proxyBeanMethods = false)
@Profile("course-demo")
public class CourseDemoSecurity {
    @Bean
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService demoUsers(@Value("${COURSE_DEMO_PASSWORD}") String password) {
        if (password.isBlank())
            throw new IllegalArgumentException("COURSE_DEMO_PASSWORD must not be blank");
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new InMemoryUserDetailsManager(
                User.withUsername("academic.demo")
                        .password(encoder.encode(password))
                        .authorities("ROLE_ACADEMIC_STAFF", "SCOPE_ACADEMIC_STAFF|GLOBAL|*")
                        .build());
    }

    @Bean
    SecurityFilterChain demoFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
