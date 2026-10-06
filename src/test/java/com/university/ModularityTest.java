package com.university;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {
    @Test
    void modulesDoNotAccessEachOthersInternals() {
        ApplicationModules.of(UniversityApplication.class).verify();
    }
}
