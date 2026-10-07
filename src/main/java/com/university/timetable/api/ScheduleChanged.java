package com.university.timetable.api;

public record ScheduleChanged(String timetableSessionId, String teachingClassId, String reason) {}
