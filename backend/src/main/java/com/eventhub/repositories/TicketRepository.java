package com.eventhub.repositories;

import java.util.Optional;

import com.eventhub.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);
}
