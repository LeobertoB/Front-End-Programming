package com.eventhub.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import com.eventhub.domain.entities.AppUser;
import com.eventhub.domain.entities.Category;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.Role;
import com.eventhub.domain.entities.Venue;
import com.eventhub.domain.enums.EventStatus;
import com.eventhub.domain.enums.RoleName;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.CategoryRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.RoleRepository;
import com.eventhub.repositories.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final AppUserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            CategoryRepository categoryRepository,
            VenueRepository venueRepository,
            AppUserRepository userRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Arrays.stream(RoleName.values())
                .filter(roleName -> roleRepository.findByName(roleName).isEmpty())
                .map(Role::new)
                .forEach(roleRepository::save);

        seedCategories();
        seedVenues();
        seedEvents();
    }

    private void seedCategories() {
        ensureCategory("Concerts", "Live performances, festivals, and concerts.");
        ensureCategory("Technology", "Workshops, conferences, and innovation meetups.");
        ensureCategory("Food", "Tastings, culinary experiences, and local markets.");
        ensureCategory("Arts", "Gallery openings, design showcases, and creative talks.");
        ensureCategory("Business", "Networking events, leadership panels, and startup sessions.");
    }

    private void seedVenues() {
        ensureVenue("Central Hall", "Rome", "Italy", "Via Roma 10", 500, 41.9028, 12.4964);
        ensureVenue("Riverside Studio", "Milan", "Italy", "Via Naviglio 22", 220, 45.4642, 9.1900);
        ensureVenue("Open Air Garden", "Florence", "Italy", "Piazza Verde 4", 350, 43.7696, 11.2558);
        ensureVenue("Harbor Conference Center", "Naples", "Italy", "Molo Beverello 8", 420, 40.8518, 14.2681);
        ensureVenue("Nord Innovation Hub", "Turin", "Italy", "Corso Regina 88", 180, 45.0703, 7.6869);
    }

    private void seedEvents() {
        if (eventRepository.count() > 0) {
            return;
        }

        AppUser organizer = ensureDemoOrganizer();
        LocalDateTime baseDate = LocalDateTime.now().plusDays(14).withMinute(0).withSecond(0).withNano(0);

        eventRepository.save(event(
                organizer,
                "Rome Summer Sessions",
                "An open-air evening with live bands, local food vendors, and curated guest experiences.",
                baseDate.plusDays(2).withHour(20),
                BigDecimal.valueOf(32.00),
                180,
                "https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=1200&q=80",
                "Central Hall",
                "Concerts",
                EventStatus.PUBLISHED
        ));
        eventRepository.save(event(
                organizer,
                "Frontend Leaders Summit",
                "A practical conference for product teams covering design systems, performance, and modern frontend architecture.",
                baseDate.plusDays(7).withHour(9),
                BigDecimal.valueOf(89.00),
                120,
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80",
                "Nord Innovation Hub",
                "Technology",
                EventStatus.PUBLISHED
        ));
        eventRepository.save(event(
                organizer,
                "Florence Makers Market",
                "A weekend market for independent makers, seasonal tastings, and small creative workshops.",
                baseDate.plusDays(11).withHour(11),
                BigDecimal.valueOf(12.50),
                240,
                "https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&w=1200&q=80",
                "Open Air Garden",
                "Food",
                EventStatus.PUBLISHED
        ));
        eventRepository.save(event(
                organizer,
                "Milan Product Design Lab",
                "A draft planning session for a hands-on workshop about research, prototyping, and product storytelling.",
                baseDate.plusDays(16).withHour(18),
                BigDecimal.valueOf(45.00),
                60,
                "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=80",
                "Riverside Studio",
                "Arts",
                EventStatus.DRAFT
        ));
    }

    private Category ensureCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(category(name, description)));
    }

    private Venue ensureVenue(
            String name,
            String city,
            String country,
            String address,
            Integer capacity,
            Double latitude,
            Double longitude
    ) {
        return venueRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> venueRepository.save(venue(name, city, country, address, capacity, latitude, longitude)));
    }

    private AppUser ensureDemoOrganizer() {
        return userRepository.findByEmailIgnoreCase("organizer.demo@eventhub.local")
                .orElseGet(() -> {
                    Role organizerRole = roleRepository.findByName(RoleName.ROLE_ORGANIZER)
                            .orElseThrow(() -> new IllegalStateException("Organizer role not initialized"));

                    AppUser user = new AppUser();
                    user.setEmail("organizer.demo@eventhub.local");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setName("Demo");
                    user.setSurname("Organizer");
                    user.setProfileImageUrl("https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=400&q=80");
                    user.setCity("Rome");
                    user.setFavoriteEventType("Concerts");
                    user.setRoles(Set.of(organizerRole));
                    return userRepository.save(user);
                });
    }

    private Event event(
            AppUser organizer,
            String title,
            String description,
            LocalDateTime startsAt,
            BigDecimal basePrice,
            Integer availableSeats,
            String imageUrl,
            String venueName,
            String categoryName,
            EventStatus status
    ) {
        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(title);
        event.setDescription(description);
        event.setStartsAt(startsAt);
        event.setEndsAt(startsAt.plusHours(3));
        event.setBasePrice(basePrice);
        event.setAvailableSeats(availableSeats);
        event.setImageUrl(imageUrl);
        event.setVenue(venueRepository.findByNameIgnoreCase(venueName)
                .orElseThrow(() -> new IllegalStateException("Seed venue not found: " + venueName)));
        event.setCategory(categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new IllegalStateException("Seed category not found: " + categoryName)));
        event.setStatus(status);
        return event;
    }

    private Category category(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    private Venue venue(
            String name,
            String city,
            String country,
            String address,
            Integer capacity,
            Double latitude,
            Double longitude
    ) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setCity(city);
        venue.setCountry(country);
        venue.setAddress(address);
        venue.setCapacity(capacity);
        venue.setLatitude(latitude);
        venue.setLongitude(longitude);
        return venue;
    }
}
