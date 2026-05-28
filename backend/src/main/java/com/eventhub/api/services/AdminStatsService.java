package com.eventhub.api.services;

import java.math.BigDecimal;

import com.eventhub.api.dto.AdminStatsResponse;
import com.eventhub.domain.enums.BookingStatus;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStatsService {

    private final AppUserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public AdminStatsService(
            AppUserRepository userRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        BigDecimal revenue = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .map(booking -> booking.getTotalPrice() == null ? BigDecimal.ZERO : booking.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double averageRating = reviewRepository.findAll().stream()
                .mapToInt(review -> review.getRating() == null ? 0 : review.getRating())
                .average()
                .orElse(0);

        return new AdminStatsResponse(
                userRepository.count(),
                eventRepository.count(),
                bookingRepository.count(),
                bookingRepository.findAll().stream()
                        .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                        .count(),
                revenue,
                averageRating
        );
    }
}
