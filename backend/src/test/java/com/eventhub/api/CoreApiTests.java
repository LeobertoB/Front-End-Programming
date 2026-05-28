package com.eventhub.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CoreApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullEventFlowSupportsCrudFiltersBookingReviewAndStats() throws Exception {
        String adminToken = register("stage4-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("stage4-organizer@example.com", "ROLE_ORGANIZER");
        String userToken = register("stage4-user@example.com", "ROLE_USER");

        Long categoryId = createCategory(adminToken, "Music");
        Long venueId = createVenue(adminToken, "Rome");
        Long eventId = createEvent(organizerToken, categoryId, venueId, "Published Jazz Night", "PUBLISHED");

        mockMvc.perform(get("/api/events")
                        .param("city", "rome")
                        .param("categoryId", categoryId.toString())
                        .param("status", "PUBLISHED")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "startsAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId))
                .andExpect(jsonPath("$.content[0].venue.city").value("Rome"))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": %d,
                                  "quantity": 2
                                }
                                """.formatted(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(50.00));

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": %d,
                                  "rating": 5,
                                  "comment": "Excellent organization."
                                }
                                """.formatted(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/events/{eventId}/reviews", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Excellent organization."));

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.confirmedBookings").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.averageRating").value(greaterThanOrEqualTo(5.0)));
    }

    @Test
    void validationErrorsUseStructuredPayload() throws Exception {
        String adminToken = register("stage4-validation-admin@example.com", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").isString())
                .andExpect(jsonPath("$.validationErrors.description").isString());
    }

    @Test
    void organizerManagedEventsIncludeDrafts() throws Exception {
        String adminToken = register("stage4-draft-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("stage4-draft-organizer@example.com", "ROLE_ORGANIZER");

        Long categoryId = createCategory(adminToken, "Draft Category");
        Long venueId = createVenue(adminToken, "Florence");
        Long eventId = createEvent(organizerToken, categoryId, venueId, "Private Draft Event", "DRAFT");

        mockMvc.perform(get("/api/organizer/events")
                        .header("Authorization", bearer(organizerToken))
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));
    }

    @Test
    void userCanTrackReviewsAndOrganizerCanReply() throws Exception {
        String adminToken = register("stage4-review-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("stage4-review-organizer@example.com", "ROLE_ORGANIZER");
        String userToken = register("stage4-review-user@example.com", "ROLE_USER");

        Long categoryId = createCategory(adminToken, "Review Category");
        Long venueId = createVenue(adminToken, "Naples");
        Long eventId = createEvent(organizerToken, categoryId, venueId, "Reviewed Event", "PUBLISHED");
        Long reviewId = createReview(userToken, eventId);

        mockMvc.perform(get("/api/reviews/me")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reviewId))
                .andExpect(jsonPath("$[0].eventTitle").value("Reviewed Event"))
                .andExpect(jsonPath("$[0].comment").value("Excellent organization."));

        mockMvc.perform(get("/api/reviews/manageable")
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reviewId))
                .andExpect(jsonPath("$[0].comment").value("Excellent organization."));

        mockMvc.perform(put("/api/reviews/{reviewId}/reply", reviewId)
                        .header("Authorization", bearer(organizerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reply": "Thank you for joining us. We are glad you enjoyed the event."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officialReply").value("Thank you for joining us. We are glad you enjoyed the event."))
                .andExpect(jsonPath("$.repliedByName").value("Test User"));

        mockMvc.perform(get("/api/reviews/manageable")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officialReply").value("Thank you for joining us. We are glad you enjoyed the event."));

        mockMvc.perform(get("/api/reviews/me")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officialReply").value("Thank you for joining us. We are glad you enjoyed the event."));

        mockMvc.perform(put("/api/reviews/{reviewId}/follow-up", reviewId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "followUp": "Thanks for the clarification. I will join again."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userFollowUp").value("Thanks for the clarification. I will join again."));
    }

    @Test
    void regularUserCannotCreateAdminCatalogData() throws Exception {
        String userToken = register("stage4-blocked-user@example.com", "ROLE_USER");

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Blocked",
                                  "description": "Should fail"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Access denied")));
    }

    @Test
    void userCanUploadProfileImage() throws Exception {
        String userToken = register("stage4-profile-upload@example.com", "ROLE_USER");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/profile-image/upload")
                        .file(file)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl", containsString("/uploads/profiles/")));
    }

    @Test
    void organizerCanUploadEventImage() throws Exception {
        String adminToken = register("stage4-event-upload-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("stage4-event-upload-organizer@example.com", "ROLE_ORGANIZER");
        Long categoryId = createCategory(adminToken, "Upload Category");
        Long venueId = createVenue(adminToken, "Turin");
        Long eventId = createEvent(organizerToken, categoryId, venueId, "Upload Cover Event", "PUBLISHED");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.webp",
                "image/webp",
                "fake image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/organizer/events/{id}/image/upload", eventId)
                        .file(file)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", containsString("/uploads/events/")));
    }

    @Test
    void organizerCanUploadEventGalleryImages() throws Exception {
        String adminToken = register("stage4-gallery-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("stage4-gallery-organizer@example.com", "ROLE_ORGANIZER");
        Long categoryId = createCategory(adminToken, "Gallery Category");
        Long venueId = createVenue(adminToken, "Bologna");
        Long eventId = createEvent(organizerToken, categoryId, venueId, "Gallery Event", "PUBLISHED");
        MockMultipartFile first = new MockMultipartFile("files", "first.jpg", MediaType.IMAGE_JPEG_VALUE, "first".getBytes());
        MockMultipartFile second = new MockMultipartFile("files", "second.png", MediaType.IMAGE_PNG_VALUE, "second".getBytes());

        mockMvc.perform(multipart("/api/organizer/events/{id}/gallery", eventId)
                        .file(first)
                        .file(second)
                        .header("Authorization", bearer(organizerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.galleryImages[0].imageUrl", containsString("/uploads/events-gallery/")))
                .andExpect(jsonPath("$.galleryImages[1].imageUrl", containsString("/uploads/events-gallery/")));
    }

    private Long createCategory(String token, String name) throws Exception {
        String response = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Live performances and concerts"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createVenue(String token, String city) throws Exception {
        String response = mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Central Hall",
                                  "city": "%s",
                                  "country": "Italy",
                                  "address": "Via Roma 10",
                                  "capacity": 500,
                                  "latitude": 41.9028,
                                  "longitude": 12.4964
                                }
                                """.formatted(city)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createEvent(String token, Long categoryId, Long venueId, String title, String status) throws Exception {
        LocalDateTime startsAt = LocalDateTime.now().plusDays(30).withNano(0);
        LocalDateTime endsAt = startsAt.plusHours(3);
        String response = mockMvc.perform(post("/api/organizer/events")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "A curated night of live music.",
                                  "startsAt": "%s",
                                  "endsAt": "%s",
                                  "basePrice": 25.00,
                                  "availableSeats": 100,
                                  "imageUrl": "https://example.com/event.jpg",
                                  "venueId": %d,
                                  "categoryId": %d,
                                  "status": "%s"
                                }
                                """.formatted(title, startsAt, endsAt, venueId, categoryId, status)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createReview(String token, Long eventId) throws Exception {
        String response = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": %d,
                                  "rating": 5,
                                  "comment": "Excellent organization."
                                }
                                """.formatted(eventId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String register(String email, String role) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123",
                                  "name": "Test",
                                  "surname": "User",
                                  "profileImageUrl": "https://example.com/profile.jpg",
                                  "role": "%s"
                                }
                                """.formatted(email, role)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
