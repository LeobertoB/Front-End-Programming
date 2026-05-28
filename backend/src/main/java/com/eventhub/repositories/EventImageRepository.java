package com.eventhub.repositories;

import java.util.Optional;

import com.eventhub.domain.entities.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    Optional<EventImage> findByIdAndEventId(Long id, Long eventId);
}
