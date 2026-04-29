package com.contacts.integration;

import com.contacts.dto.AuthResponse;
import com.contacts.dto.ContactResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Contact Management Integration Tests")
class ContactManagementIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Shared state across ordered tests
    private static String jwtToken;
    private static Long createdContactId;

    // -------------------------------------------------------------------------
    // Auth Flow
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Register: returns 201 for new user")
    void register_returns201_forNewUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", "integrationuser",
                                "email", "integration@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @Order(2)
    @DisplayName("Register: returns 409 when registering with same email again")
    void register_returns409_whenDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", "otheruser",
                                "email", "integration@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("Login: returns JWT token for valid credentials")
    void login_returnsJwtToken_forValidCredentials() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "integration@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        jwtToken = response.getToken();
        assertThat(jwtToken).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("Login: returns 401 for wrong password")
    void login_returns401_forWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "integration@test.com",
                                "password", "wrongpassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Security
    // -------------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("Security: returns 401 when accessing contacts without token")
    void contacts_returns401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Contact CRUD Flow
    // -------------------------------------------------------------------------

    @Test
    @Order(6)
    @DisplayName("Create Contact: returns 201 with created contact")
    void createContact_returns201() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "firstName", "Jane",
                                "lastName", "Doe",
                                "email", "jane@mail.com",
                                "phone", "+1234567890",
                                "address", "New York",
                                "contactGroup", "FRIENDS"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.contactGroup").value("FRIENDS"))
                .andReturn();

        ContactResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContactResponse.class);
        createdContactId = response.getId();
        assertThat(createdContactId).isNotNull();
    }

    @Test
    @Order(7)
    @DisplayName("Create Contact: returns 400 when firstName is missing")
    void createContact_returns400_whenFirstNameMissing() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "jane@mail.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    @Order(8)
    @DisplayName("Get All Contacts: returns list with the created contact")
    void getAllContacts_returnsCreatedContact() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Jane"));
    }

    @Test
    @Order(9)
    @DisplayName("Get Contact By ID: returns correct contact")
    void getContactById_returnsCorrectContact() throws Exception {
        mockMvc.perform(get("/api/contacts/" + createdContactId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdContactId))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @Order(10)
    @DisplayName("Update Contact: returns updated contact")
    void updateContact_returnsUpdatedContact() throws Exception {
        mockMvc.perform(put("/api/contacts/" + createdContactId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "firstName", "Janet",
                                "lastName", "Doe",
                                "email", "janet@mail.com",
                                "phone", "+9876543210",
                                "address", "Los Angeles",
                                "contactGroup", "WORK"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Janet"))
                .andExpect(jsonPath("$.contactGroup").value("WORK"));
    }

    @Test
    @Order(11)
    @DisplayName("Search Contacts: returns matching contacts by name")
    void searchContacts_returnsMatchingContacts() throws Exception {
        mockMvc.perform(get("/api/contacts/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("keyword", "janet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Janet"));
    }

    @Test
    @Order(12)
    @DisplayName("Search Contacts: returns empty list when no match")
    void searchContacts_returnsEmpty_whenNoMatch() throws Exception {
        mockMvc.perform(get("/api/contacts/search")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("keyword", "zzznomatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("Get Contact By ID: returns 404 for another user's contact")
    void getContactById_returns404_forOtherUsersContact() throws Exception {
        // Register and login as a second user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "username", "seconduser",
                        "email", "second@test.com",
                        "password", "password123"
                )))).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "second@test.com",
                        "password", "password123"
                )))).andReturn();

        String secondToken = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AuthResponse.class).getToken();

        // Try to access first user's contact with second user's token
        mockMvc.perform(get("/api/contacts/" + createdContactId)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    @DisplayName("Delete Contact: returns 200 with success message")
    void deleteContact_returns200() throws Exception {
        mockMvc.perform(delete("/api/contacts/" + createdContactId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contact deleted successfully"));
    }

    @Test
    @Order(15)
    @DisplayName("Get Contact By ID: returns 404 after deletion")
    void getContactById_returns404_afterDeletion() throws Exception {
        mockMvc.perform(get("/api/contacts/" + createdContactId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
