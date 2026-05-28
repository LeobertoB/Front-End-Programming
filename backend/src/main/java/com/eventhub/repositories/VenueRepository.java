package com.eventhub.repositories;

import java.util.List;
import java.util.Optional;

import com.eventhub.domain.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByCityIgnoreCase(String city);

    Optional<Venue> findByNameIgnoreCase(String name);
}
