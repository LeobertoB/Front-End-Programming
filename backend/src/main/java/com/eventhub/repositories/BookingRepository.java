package com.eventhub.repositories;

import java.util.List;

import com.eventhub.domain.entities.Booking;
import com.eventhub.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByEventIdAndStatus(Long eventId, BookingStatus status);
}
