@org.springframework.modulith.ApplicationModule(
        displayName = "Student attendance",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "student :: api",
            "enrollment :: api",
            "timetable :: api",
            "teachingclass :: api"
        })
package com.university.attendance;
