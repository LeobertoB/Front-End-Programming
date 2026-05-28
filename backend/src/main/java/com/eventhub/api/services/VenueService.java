package com.eventhub.api.services;

import java.util.List;

import com.eventhub.api.dto.VenueRequest;
import com.eventhub.api.dto.VenueResponse;
import com.eventhub.api.mappers.ApiMapper;
import com.eventhub.domain.entities.Venue;
import com.eventhub.repositories.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findAll(String city) {
        List<Venue> venues = city == null || city.isBlank()
                ? venueRepository.findAll()
                : venueRepository.findByCityIgnoreCase(city);
        return venues.stream().map(ApiMapper::toVenue).toList();
    }

    @Transactional
    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue();
        apply(venue, request);
        return ApiMapper.toVenue(venueRepository.save(venue));
    }

    @Transactional
    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = getEntity(id);
        apply(venue, request);
        return ApiMapper.toVenue(venue);
    }

    @Transactional
    public void delete(Long id) {
        venueRepository.delete(getEntity(id));
    }

    public Venue getEntity(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));
    }

    private void apply(Venue venue, VenueRequest request) {
        venue.setName(request.name().trim());
        venue.setCity(request.city().trim());
        venue.setCountry(request.country().trim());
        venue.setAddress(request.address().trim());
        venue.setCapacity(request.capacity());
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());
    }
}
