package com.university.organization.internal.application;

import com.university.organization.api.*;
import com.university.organization.internal.domain.Room;
import com.university.organization.internal.persistence.RoomRepository;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RoomCommands {
    private final RoomRepository repository;
    private final TechnicalAudit audit;

    public RoomCommands(RoomRepository repository, TechnicalAudit audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public RoomView create(RoomRequest r) {

        Access.require("ROOM_MANAGER", "GLOBAL", "*");
        var e = new Room(UUID.randomUUID().toString(), r.code(), r.building(), r.capacity());
        repository.saveAndFlush(e);
        audit.record("organization", e.getId(), "CREATE_ROOM", r.toString());
        return view(e);
    }

    public RoomView update(String id, RoomRequest r) {
        var e = repository.findById(id).orElseThrow(() -> BusinessException.missing("Room"));

        Access.require("ROOM_MANAGER", "ROOM", id);
        e.revise(r.code(), r.building(), r.capacity(), r.version());
        repository.flush();
        audit.record("organization", id, "UPDATE_ROOM", r.toString());
        return view(e);
    }

    private RoomView view(Room e) {
        return new RoomView(
                e.getId(), e.getCode(), e.getBuilding(), e.getCapacity(), e.getVersion());
    }
}
