@org.springframework.modulith.ApplicationModule(
        displayName = "Read-only reports",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "course :: api",
            "student :: api",
            "teachingclass :: api",
            "enrollment :: api",
            "tuition :: api",
            "attendance :: api",
            "teaching :: api",
            "grade :: api"
        })
package com.university.reporting;
