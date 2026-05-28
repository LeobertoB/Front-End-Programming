package com.eventhub.api.controllers;

import java.util.List;

import com.eventhub.api.dto.VenueRequest;
import com.eventhub.api.dto.VenueResponse;
import com.eventhub.api.services.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/api/venues")
    public List<VenueResponse> findAll(@RequestParam(required = false) String city) {
        return venueService.findAll(city);
    }

    @PostMapping("/api/admin/venues")
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse create(@Valid @RequestBody VenueRequest request) {
        return venueService.create(request);
    }

    @PutMapping("/api/admin/venues/{id}")
    public VenueResponse update(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return venueService.update(id, request);
    }

    @DeleteMapping("/api/admin/venues/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }
}
