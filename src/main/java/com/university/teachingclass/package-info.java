@org.springframework.modulith.ApplicationModule(
        displayName = "Course offerings and assignments",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "academic :: api",
            "course :: api",
            "lecturer :: api",
            "organization :: api"
        })
package com.university.teachingclass;
