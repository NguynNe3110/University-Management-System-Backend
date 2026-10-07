@org.springframework.modulith.ApplicationModule(
        displayName = "Teaching declarations and confirmation",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "lecturer :: api",
            "timetable :: api",
            "teachingclass :: api"
        })
package com.university.teaching;
