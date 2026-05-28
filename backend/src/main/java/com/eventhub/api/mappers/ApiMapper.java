package com.eventhub.api.mappers;

import com.eventhub.api.dto.BookingResponse;
import com.eventhub.api.dto.CategoryResponse;
import com.eventhub.api.dto.EventImageResponse;
import com.eventhub.api.dto.EventResponse;
import com.eventhub.api.dto.ReviewResponse;
import com.eventhub.api.dto.VenueResponse;
import com.eventhub.domain.entities.Booking;
import com.eventhub.domain.entities.Category;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.Review;
import com.eventhub.domain.entities.Venue;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static CategoryResponse toCategory(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public static VenueResponse toVenue(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                venue.getCountry(),
                venue.getAddress(),
                venue.getCapacity(),
                venue.getLatitude(),
                venue.getLongitude()
        );
    }

    public static EventResponse toEvent(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getBasePrice(),
                event.getAvailableSeats(),
                event.getImageUrl(),
                event.getStatus(),
                event.getOrganizer().getId(),
                event.getOrganizer().getName() + " " + event.getOrganizer().getSurname(),
                toCategory(event.getCategory()),
                toVenue(event.getVenue()),
                event.getGalleryImages().stream()
                        .map(image -> new EventImageResponse(image.getId(), image.getImageUrl()))
                        .toList()
        );
    }

    public static BookingResponse toBooking(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getQuantity(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }

    public static ReviewResponse toReview(Review review) {
        String repliedByName = review.getRepliedBy() == null
                ? null
                : review.getRepliedBy().getName() + " " + review.getRepliedBy().getSurname();

        return new ReviewResponse(
                review.getId(),
                review.getEvent().getId(),
                review.getEvent().getTitle(),
                review.getUser().getId(),
                review.getUser().getName() + " " + review.getUser().getSurname(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getOfficialReply(),
                review.getRepliedAt(),
                review.getRepliedBy() == null ? null : review.getRepliedBy().getId(),
                repliedByName,
                review.getUserFollowUp(),
                review.getUserFollowedUpAt()
        );
    }
}
