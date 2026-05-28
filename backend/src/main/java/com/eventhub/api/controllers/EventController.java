package com.eventhub.api.controllers;

import java.time.LocalDateTime;
import java.util.List;

import com.eventhub.api.dto.EventRequest;
import com.eventhub.api.dto.EventResponse;
import com.eventhub.api.dto.PageResponse;
import com.eventhub.api.services.EventService;
import com.eventhub.domain.enums.EventStatus;
import com.eventhub.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/api/events")
    public PageResponse<EventResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsBefore,
            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable
    ) {
        return PageResponse.from(eventService.search(city, categoryId, status, startsAfter, startsBefore, pageable));
    }

    @GetMapping("/api/events/{id}")
    public EventResponse get(@PathVariable Long id) {
        return eventService.get(id);
    }

    @GetMapping("/api/organizer/events")
    public PageResponse<EventResponse> findManaged(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 50, sort = "startsAt") Pageable pageable
    ) {
        return PageResponse.from(eventService.findManaged(principal, pageable));
    }

    @PostMapping("/api/organizer/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return eventService.create(request, principal);
    }

    @PutMapping("/api/organizer/events/{id}")
    public EventResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return eventService.update(id, request, principal);
    }

    @PostMapping("/api/organizer/events/{id}/image/upload")
    public EventResponse uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return eventService.uploadImage(id, file, principal);
    }

    @PostMapping("/api/organizer/events/{id}/gallery")
    public EventResponse uploadGalleryImages(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return eventService.uploadGalleryImages(id, files, principal);
    }

    @DeleteMapping("/api/organizer/events/{id}/gallery/{imageId}")
    public EventResponse deleteGalleryImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return eventService.deleteGalleryImage(id, imageId, principal);
    }

    @DeleteMapping("/api/organizer/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        eventService.delete(id, principal);
    }
}
