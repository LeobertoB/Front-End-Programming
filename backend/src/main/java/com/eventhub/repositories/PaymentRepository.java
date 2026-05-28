package com.eventhub.repositories;

import java.util.Optional;

import com.eventhub.domain.entities.Payment;
import com.eventhub.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    long countByStatus(PaymentStatus status);
}
