package com.eventhub.integrations;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class ThirdPartyIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void eventWeatherUsesOpenMeteoForecastData() throws Exception {
        String adminToken = register("weather-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("weather-organizer@example.com", "ROLE_ORGANIZER");
        Long categoryId = createCategory(adminToken, "Outdoor Weather");
        Long venueId = createVenue(adminToken);
        Long eventId = createEvent(organizerToken, categoryId, venueId);

        mockServer.expect(once(), requestTo(Matchers.containsString("https://api.open-meteo.test/v1/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "hourly": {
                            "time": ["2026-06-10T18:00", "2026-06-10T20:00", "2026-06-10T22:00"],
                            "temperature_2m": [18.1, 21.5, 19.4],
                            "weather_code": [2, 0, 3],
                            "wind_speed_10m": [8.0, 6.4, 7.2]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/events/{eventId}/weather", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.city").value("Rome"))
                .andExpect(jsonPath("$.temperatureCelsius").value(21.5))
                .andExpect(jsonPath("$.weatherCode").value(0))
                .andExpect(jsonPath("$.summary", containsString("clear sky")));

        mockServer.verify();
    }

    @Test
    void countryInfoUsesRestCountriesData() throws Exception {
        mockServer.expect(once(), requestTo("https://rest-countries.test/v3.1/name/Italy?fullText=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {
                            "name": {
                              "common": "Italy",
                              "official": "Italian Republic"
                            },
                            "region": "Europe",
                            "subregion": "Southern Europe",
                            "capital": ["Rome"],
                            "currencies": {
                              "EUR": {
                                "name": "Euro",
                                "symbol": "€"
                              }
                            },
                            "languages": {
                              "ita": "Italian"
                            },
                            "flag": "🇮🇹"
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/venues/country-info").param("country", "Italy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonName").value("Italy"))
                .andExpect(jsonPath("$.officialName").value("Italian Republic"))
                .andExpect(jsonPath("$.capital[0]").value("Rome"))
                .andExpect(jsonPath("$.currencies[0]").value("EUR"))
                .andExpect(jsonPath("$.languages[0]").value("Italian"));

        mockServer.verify();
    }

    @Test
    void weatherEndpointRequiresVenueCoordinates() throws Exception {
        String adminToken = register("weather-missing-admin@example.com", "ROLE_ADMIN");
        String organizerToken = register("weather-missing-organizer@example.com", "ROLE_ORGANIZER");
        Long categoryId = createCategory(adminToken, "Outdoor Missing Coordinates");
        Long venueId = createVenueWithoutCoordinates(adminToken);
        Long eventId = createEvent(organizerToken, categoryId, venueId);

        mockMvc.perform(get("/api/events/{eventId}/weather", eventId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Event venue does not have coordinates for weather lookup"));
    }

    private Long createCategory(String token, String name) throws Exception {
        String response = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Outdoor cultural events"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createVenue(String token) throws Exception {
        String response = mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "River Stage",
                                  "city": "Rome",
                                  "country": "Italy",
                                  "address": "Lungotevere 1",
                                  "capacity": 300,
                                  "latitude": 41.9028,
                                  "longitude": 12.4964
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createVenueWithoutCoordinates(String token) throws Exception {
        String response = mockMvc.perform(post("/api/admin/venues")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Indoor Hall",
                                  "city": "Rome",
                                  "country": "Italy",
                                  "address": "Via Test 4",
                                  "capacity": 120
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createEvent(String token, Long categoryId, Long venueId) throws Exception {
        LocalDateTime startsAt = LocalDateTime.of(2026, 6, 10, 20, 0);
        String response = mockMvc.perform(post("/api/organizer/events")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Summer Night Market",
                                  "description": "Food, music, and local makers.",
                                  "startsAt": "%s",
                                  "endsAt": "%s",
                                  "basePrice": 12.00,
                                  "availableSeats": 80,
                                  "imageUrl": "https://example.com/market.jpg",
                                  "venueId": %d,
                                  "categoryId": %d,
                                  "status": "PUBLISHED"
                                }
                                """.formatted(startsAt, startsAt.plusHours(4), venueId, categoryId)))
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
                                  "name": "Integration",
                                  "surname": "Tester",
                                  "profileImageUrl": "https://example.com/profile.jpg",
                                  "role": "%s"
                                }
                                """.formatted(email, role)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
