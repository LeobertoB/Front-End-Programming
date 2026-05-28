package com.eventhub.api.services;

import java.time.Instant;
import java.util.List;

import com.eventhub.api.dto.ReviewFollowUpRequest;
import com.eventhub.api.dto.ReviewRequest;
import com.eventhub.api.dto.ReviewReplyRequest;
import com.eventhub.api.dto.ReviewResponse;
import com.eventhub.api.mappers.ApiMapper;
import com.eventhub.domain.entities.AppUser;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.Review;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.ReviewRepository;
import com.eventhub.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppUserRepository userRepository;
    private final EventService eventService;

    public ReviewService(
            ReviewRepository reviewRepository,
            AppUserRepository userRepository,
            EventService eventService
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findForEvent(Long eventId) {
        return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(ApiMapper::toReview)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findMine(UserPrincipal principal) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(ApiMapper::toReview)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findManageable(UserPrincipal principal) {
        boolean admin = hasRole(principal, "ROLE_ADMIN");
        boolean organizer = hasRole(principal, "ROLE_ORGANIZER");
        if (!admin && !organizer) {
            throw new AccessDeniedException("Only organizers or admins can manage reviews");
        }

        List<Review> reviews = admin
                ? reviewRepository.findAllByOrderByCreatedAtDesc()
                : reviewRepository.findByEventOrganizerIdOrderByCreatedAtDesc(principal.getId());

        return reviews.stream()
                .map(ApiMapper::toReview)
                .toList();
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request, UserPrincipal principal) {
        reviewRepository.findByUserIdAndEventId(principal.getId(), request.eventId()).ifPresent(existing -> {
            throw new IllegalArgumentException("You have already reviewed this event");
        });

        AppUser user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Event event = eventService.getEntity(request.eventId());

        Review review = new Review();
        review.setUser(user);
        review.setEvent(event);
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        return ApiMapper.toReview(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse reply(Long reviewId, ReviewReplyRequest request, UserPrincipal principal) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        AppUser responder = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean admin = hasRole(principal, "ROLE_ADMIN");
        boolean organizer = review.getEvent().getOrganizer().getId().equals(principal.getId());
        if (!admin && !organizer) {
            throw new AccessDeniedException("Only the event organizer or an admin can reply to this review");
        }

        review.setOfficialReply(request.reply().trim());
        review.setRepliedAt(Instant.now());
        review.setRepliedBy(responder);
        return ApiMapper.toReview(review);
    }

    @Transactional
    public ReviewResponse followUp(Long reviewId, ReviewFollowUpRequest request, UserPrincipal principal) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("Only the review author can reply to the official response");
        }
        if (review.getOfficialReply() == null || review.getOfficialReply().isBlank()) {
            throw new IllegalArgumentException("An official reply is required before adding a follow-up");
        }

        review.setUserFollowUp(request.followUp().trim());
        review.setUserFollowedUpAt(Instant.now());
        return ApiMapper.toReview(review);
    }

    private boolean hasRole(UserPrincipal principal, String role) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
