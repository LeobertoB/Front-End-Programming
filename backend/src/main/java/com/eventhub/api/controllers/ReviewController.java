package com.eventhub.api.controllers;

import java.util.List;

import com.eventhub.api.dto.ReviewFollowUpRequest;
import com.eventhub.api.dto.ReviewRequest;
import com.eventhub.api.dto.ReviewReplyRequest;
import com.eventhub.api.dto.ReviewResponse;
import com.eventhub.api.services.ReviewService;
import com.eventhub.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/events/{eventId}/reviews")
    public List<ReviewResponse> findForEvent(@PathVariable Long eventId) {
        return reviewService.findForEvent(eventId);
    }

    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reviewService.create(request, principal);
    }

    @GetMapping("/api/reviews/me")
    public List<ReviewResponse> findMine(@AuthenticationPrincipal UserPrincipal principal) {
        return reviewService.findMine(principal);
    }

    @GetMapping("/api/reviews/manageable")
    public List<ReviewResponse> findManageable(@AuthenticationPrincipal UserPrincipal principal) {
        return reviewService.findManageable(principal);
    }

    @PutMapping("/api/reviews/{reviewId}/reply")
    public ReviewResponse reply(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReplyRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reviewService.reply(reviewId, request, principal);
    }

    @PutMapping("/api/reviews/{reviewId}/follow-up")
    public ReviewResponse followUp(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewFollowUpRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reviewService.followUp(reviewId, request, principal);
    }
}
