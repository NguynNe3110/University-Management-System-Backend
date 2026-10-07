@org.springframework.modulith.ApplicationModule(
        displayName = "Tuition and payments",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "student :: api",
            "enrollment :: api",
            "teachingclass :: api",
            "academic :: api"
        })
package com.university.tuition;
