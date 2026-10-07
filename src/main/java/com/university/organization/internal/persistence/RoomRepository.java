package com.university.organization.internal.persistence;

import com.university.organization.internal.domain.Room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Room r where r.id=:id")
    Optional<Room> lockById(@org.springframework.data.repository.query.Param("id") String id);

    Optional<Room> findByCode(String code);
}
