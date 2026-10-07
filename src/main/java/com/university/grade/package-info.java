@org.springframework.modulith.ApplicationModule(
        displayName = "Grades and publication",
        allowedDependencies = {
            "shared :: exception",
            "shared :: security",
            "student :: api",
            "teachingclass :: api",
            "enrollment :: api"
        })
package com.university.grade;
