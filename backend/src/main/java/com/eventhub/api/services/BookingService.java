package com.eventhub.api.services;

import java.math.BigDecimal;
import java.util.List;

import com.eventhub.api.dto.BookingRequest;
import com.eventhub.api.dto.BookingResponse;
import com.eventhub.api.mappers.ApiMapper;
import com.eventhub.domain.entities.AppUser;
import com.eventhub.domain.entities.Booking;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.Ticket;
import com.eventhub.domain.enums.BookingStatus;
import com.eventhub.domain.enums.EventStatus;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final AppUserRepository userRepository;
    private final EventService eventService;

    public BookingService(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            AppUserRepository userRepository,
            EventService eventService
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional
    public BookingResponse create(BookingRequest request, UserPrincipal principal) {
        AppUser user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Event event = eventService.getEntity(request.eventId());

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Only published events can be booked");
        }
        if (event.getAvailableSeats() < request.quantity()) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        event.setAvailableSeats(event.getAvailableSeats() - request.quantity());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setQuantity(request.quantity());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(event.getBasePrice().multiply(BigDecimal.valueOf(request.quantity())));
        Booking saved = bookingRepository.save(booking);

        for (int i = 0; i < request.quantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setBooking(saved);
            ticketRepository.save(ticket);
        }

        return ApiMapper.toBooking(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findMine(UserPrincipal principal) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(ApiMapper::toBooking)
                .toList();
    }
}
