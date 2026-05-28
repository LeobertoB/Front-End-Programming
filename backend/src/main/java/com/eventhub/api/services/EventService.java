package com.eventhub.api.services;

import java.time.LocalDateTime;
import java.util.List;

import com.eventhub.api.dto.EventRequest;
import com.eventhub.api.dto.EventResponse;
import com.eventhub.api.mappers.ApiMapper;
import com.eventhub.domain.entities.AppUser;
import com.eventhub.domain.entities.Category;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.EventImage;
import com.eventhub.domain.entities.Venue;
import com.eventhub.domain.enums.EventStatus;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.EventImageRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AppUserRepository userRepository;
    private final VenueService venueService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final EventImageRepository eventImageRepository;

    public EventService(
            EventRepository eventRepository,
            AppUserRepository userRepository,
            VenueService venueService,
            CategoryService categoryService,
            FileStorageService fileStorageService,
            EventImageRepository eventImageRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.venueService = venueService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
        this.eventImageRepository = eventImageRepository;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> search(
            String city,
            Long categoryId,
            EventStatus status,
            LocalDateTime startsAfter,
            LocalDateTime startsBefore,
            Pageable pageable
    ) {
        Specification<Event> spec = Specification.where(fetchJoins())
                .and(status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status))
                .and(city == null || city.isBlank() ? null : (root, query, cb) ->
                        cb.equal(cb.lower(root.get("venue").get("city")), city.toLowerCase()))
                .and(categoryId == null ? null : (root, query, cb) ->
                        cb.equal(root.get("category").get("id"), categoryId))
                .and(startsAfter == null ? null : (root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("startsAt"), startsAfter))
                .and(startsBefore == null ? null : (root, query, cb) ->
                        cb.lessThanOrEqualTo(root.get("startsAt"), startsBefore));

        return eventRepository.findAll(spec, pageable).map(ApiMapper::toEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        return ApiMapper.toEvent(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> findManaged(UserPrincipal principal, Pageable pageable) {
        boolean admin = isAdmin(principal);
        Specification<Event> spec = Specification.where(fetchJoins())
                .and(admin ? null : (root, query, cb) ->
                        cb.equal(root.get("organizer").get("id"), principal.getId()));

        return eventRepository.findAll(spec, pageable).map(ApiMapper::toEvent);
    }

    @Transactional
    public EventResponse create(EventRequest request, UserPrincipal principal) {
        AppUser organizer = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));
        Event event = new Event();
        event.setOrganizer(organizer);
        apply(event, request);
        return ApiMapper.toEvent(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request, UserPrincipal principal) {
        Event event = getEntity(id);
        ensureOwnerOrAdmin(event, principal);
        apply(event, request);
        return ApiMapper.toEvent(event);
    }

    @Transactional
    public void delete(Long id, UserPrincipal principal) {
        Event event = getEntity(id);
        ensureOwnerOrAdmin(event, principal);
        eventRepository.delete(event);
    }

    @Transactional
    public EventResponse uploadImage(Long id, org.springframework.web.multipart.MultipartFile file, UserPrincipal principal) {
        Event event = getEntity(id);
        ensureOwnerOrAdmin(event, principal);
        event.setImageUrl(fileStorageService.storeImage(file, "events"));
        return ApiMapper.toEvent(event);
    }

    @Transactional
    public EventResponse uploadGalleryImages(Long id, List<org.springframework.web.multipart.MultipartFile> files, UserPrincipal principal) {
        Event event = getEntity(id);
        ensureOwnerOrAdmin(event, principal);
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one gallery image is required");
        }

        files.forEach(file -> {
            String imageUrl = fileStorageService.storeImage(file, "events-gallery");
            EventImage image = eventImageRepository.save(new EventImage(event, imageUrl));
            event.getGalleryImages().add(image);
        });
        return ApiMapper.toEvent(event);
    }

    @Transactional
    public EventResponse deleteGalleryImage(Long eventId, Long imageId, UserPrincipal principal) {
        Event event = getEntity(eventId);
        ensureOwnerOrAdmin(event, principal);
        EventImage image = eventImageRepository.findByIdAndEventId(imageId, eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event image not found"));
        eventImageRepository.delete(image);
        event.getGalleryImages().removeIf(current -> current.getId().equals(imageId));
        return ApiMapper.toEvent(event);
    }

    public Event getEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    private void apply(Event event, EventRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException("Event end date must be after start date");
        }
        Venue venue = venueService.getEntity(request.venueId());
        Category category = categoryService.getEntity(request.categoryId());

        event.setTitle(request.title().trim());
        event.setDescription(request.description().trim());
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setBasePrice(request.basePrice());
        event.setAvailableSeats(request.availableSeats());
        event.setImageUrl(request.imageUrl().trim());
        event.setVenue(venue);
        event.setCategory(category);
        event.setStatus(request.status() == null ? EventStatus.DRAFT : request.status());
    }

    private void ensureOwnerOrAdmin(Event event, UserPrincipal principal) {
        if (!isAdmin(principal) && !event.getOrganizer().getId().equals(principal.getId())) {
            throw new AccessDeniedException("Only the event organizer or an admin can change this event");
        }
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Specification<Event> fetchJoins() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("organizer", JoinType.LEFT);
                root.fetch("venue", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}
