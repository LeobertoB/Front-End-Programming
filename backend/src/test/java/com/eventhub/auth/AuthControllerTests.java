package com.eventhub.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndAccessProtectedUserEndpoint() throws Exception {
        String payload = """
                {
                  "email": "user@example.com",
                  "password": "password123",
                  "name": "Alex",
                  "surname": "River",
                  "profileImageUrl": "https://example.com/profile.jpg",
                  "city": "Rome",
                  "favoriteEventType": "Music",
                  "role": "ROLE_USER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/profile.jpg"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(content().string(containsString("ROLE_USER")));
    }

    @Test
    void authenticatedUserCanUpdateProfileImage() throws Exception {
        String token = registerAndReturnToken("profile-image@example.com", "ROLE_USER");

        mockMvc.perform(put("/api/users/me/profile-image")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageUrl": "https://example.com/new-profile.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/new-profile.jpg"))
                .andExpect(jsonPath("$.email").value("profile-image@example.com"));
    }

    @Test
    void regularUserCannotAccessOrganizerEndpoint() throws Exception {
        String token = registerAndReturnToken("viewer@example.com", "ROLE_USER");

        mockMvc.perform(get("/api/organizer/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerCanAccessOrganizerEndpoint() throws Exception {
        String token = registerAndReturnToken("organizer@example.com", "ROLE_ORGANIZER");

        mockMvc.perform(get("/api/organizer/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Organizer access granted"));
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        String token = registerAndReturnToken("admin@example.com", "ROLE_ADMIN");

        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin access granted"));
    }

    @Test
    void passwordIsNotReturnedInAuthResponse() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "safe@example.com",
                                  "password": "password123",
                                  "name": "Sam",
                                  "surname": "Safe",
                                  "profileImageUrl": "https://example.com/safe.jpg",
                                  "role": "ROLE_USER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.hamcrest.MatcherAssert.assertThat(response, not(containsString("password123")));
    }

    private String registerAndReturnToken(String email, String role) throws Exception {
        String payload = String.format("""
                {
                  "email": "%s",
                  "password": "password123",
                  "name": "Test",
                  "surname": "Person",
                  "profileImageUrl": "https://example.com/profile.jpg",
                  "role": "%s"
                }
                """, email, role);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
