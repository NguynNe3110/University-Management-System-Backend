@org.springframework.modulith.ApplicationModule(
        displayName = "Programs and semesters",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "organization :: api",
            "course :: api"
        })
package com.university.academic;
