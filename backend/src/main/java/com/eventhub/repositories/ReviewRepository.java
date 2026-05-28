package com.eventhub.repositories;

import java.util.List;
import java.util.Optional;

import com.eventhub.domain.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventIdOrderByCreatedAtDesc(Long eventId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByEventOrganizerIdOrderByCreatedAtDesc(Long organizerId);

    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.event.id = :eventId")
    Double averageRatingForEvent(@Param("eventId") Long eventId);
}
