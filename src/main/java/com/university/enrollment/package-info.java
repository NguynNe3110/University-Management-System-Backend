@org.springframework.modulith.ApplicationModule(
        displayName = "Course registration",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "student :: api",
            "teachingclass :: api",
            "timetable :: api",
            "academic :: api",
            "course :: api"
        })
package com.university.enrollment;
