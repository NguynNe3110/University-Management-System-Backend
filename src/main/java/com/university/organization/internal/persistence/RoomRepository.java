package com.university.organization.internal.persistence;

import com.university.organization.internal.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findByCode(String code);
}
