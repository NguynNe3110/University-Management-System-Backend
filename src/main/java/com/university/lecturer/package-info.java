@org.springframework.modulith.ApplicationModule(
        displayName = "Lecturer profiles",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "identity :: api",
            "organization :: api"
        })
package com.university.lecturer;
