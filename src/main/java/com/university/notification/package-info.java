@org.springframework.modulith.ApplicationModule(
        displayName = "In-app notifications",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "identity :: api",
            "timetable :: api",
            "enrollment :: api",
            "tuition :: api",
            "grade :: api",
            "teaching :: api"
        })
package com.university.notification;
