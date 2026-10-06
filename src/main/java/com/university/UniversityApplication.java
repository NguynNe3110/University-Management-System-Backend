package com.university;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(sharedModules = "shared")
public class UniversityApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniversityApplication.class, args);
    }
}
