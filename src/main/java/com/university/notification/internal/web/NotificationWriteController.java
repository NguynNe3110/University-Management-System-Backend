package com.university.notification.internal.web;

import com.university.notification.api.NotificationMessageView;
import com.university.notification.internal.application.NotificationCommands;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationWriteController {
    private final NotificationCommands commands;

    public NotificationWriteController(NotificationCommands commands) {
        this.commands = commands;
    }

    @PatchMapping("/{id}/read")
    public NotificationMessageView read(@PathVariable String id) {
        return commands.read(id);
    }
}
