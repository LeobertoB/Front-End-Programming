package com.eventhub.api.controllers;

import java.util.List;

import com.eventhub.api.dto.BookingRequest;
import com.eventhub.api.dto.BookingResponse;
import com.eventhub.api.services.BookingService;
import com.eventhub.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/api/bookings/me")
    public List<BookingResponse> findMine(@AuthenticationPrincipal UserPrincipal principal) {
        return bookingService.findMine(principal);
    }

    @PostMapping("/api/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return bookingService.create(request, principal);
    }
}
