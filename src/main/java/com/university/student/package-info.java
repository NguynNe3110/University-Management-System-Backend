@org.springframework.modulith.ApplicationModule(
        displayName = "Student profiles",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "identity :: api",
            "organization :: api",
            "academic :: api"
        })
package com.university.student;
