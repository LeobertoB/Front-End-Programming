package com.eventhub.repositories;

import java.time.LocalDateTime;

import com.eventhub.domain.entities.Event;
import com.eventhub.domain.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByCategoryIdAndStatus(Long categoryId, EventStatus status, Pageable pageable);

    Page<Event> findByVenueCityIgnoreCaseAndStatus(String city, EventStatus status, Pageable pageable);

    Page<Event> findByStartsAtBetweenAndStatus(
            LocalDateTime startsAfter,
            LocalDateTime startsBefore,
            EventStatus status,
            Pageable pageable
    );
}
