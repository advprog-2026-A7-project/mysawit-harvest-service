package com.mysawit.harvest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.harvest.dto.IdentityUserResponse;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(IdentityClient.class)
class IdentityClientTest {

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();
    private final String identityUrl = "http://localhost:8081";
    private final String apiKey = "secret-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(identityClient, "identityServiceUrl", identityUrl);
        ReflectionTestUtils.setField(identityClient, "internalApiKey", apiKey);
    }

    @Test
    void getUserById_Success() throws Exception {
        IdentityUserResponse mockResponse = new IdentityUserResponse();
        mockResponse.setId(userId.toString());
        mockResponse.setRole("BURUH");

        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andExpect(header("X-Internal-Api-Key", apiKey))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        IdentityUserResponse result = identityClient.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId.toString(), result.getId());
    }

    @Test
    void getUserById_ThrowsException_WhenServiceReturnsError() {
        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withServerError());

        assertThrows(UnauthorizedUserException.class, () -> identityClient.getUserById(userId));
    }

    @Test
    void getAssignedForemanId_Success() throws Exception {
        UUID foremanId = UUID.randomUUID();
        IdentityUserResponse mockResponse = new IdentityUserResponse();
        mockResponse.setRole("BURUH");
        mockResponse.setMandorId(foremanId.toString());

        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        UUID result = identityClient.getAssignedForemanId(userId);

        assertEquals(foremanId, result);
    }

    @Test
    void getAssignedForemanId_ThrowsException_WhenNotHarvester() throws Exception {
        IdentityUserResponse mockResponse = new IdentityUserResponse();
        mockResponse.setRole("MANDOR");

        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                identityClient.getAssignedForemanId(userId));

        assertEquals("User is not a harvester.", ex.getMessage());
    }

    @Test
    void getAssignedForemanId_ThrowsException_WhenNoForemanAssigned() throws Exception {
        IdentityUserResponse mockResponse = new IdentityUserResponse();
        mockResponse.setRole("BURUH");
        mockResponse.setMandorId(null);

        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                identityClient.getAssignedForemanId(userId));

        assertEquals("Harvester is not assigned to any foreman.", ex.getMessage());
    }

    @Test
    void getUserById_ThrowsException_WhenResponseIsNull() {
        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                identityClient.getUserById(userId));

        assertEquals("User identity could not be resolved.", ex.getMessage());
    }

    @Test
    void getAssignedForemanId_ThrowsException_WhenMandorIdIsBlank() throws Exception {
        IdentityUserResponse mockResponse = new IdentityUserResponse();
        mockResponse.setRole("BURUH");
        mockResponse.setMandorId("   ");

        server.expect(requestTo(identityUrl + "/api/internal/users/" + userId))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                identityClient.getAssignedForemanId(userId));

        assertEquals("Harvester is not assigned to any foreman.", ex.getMessage());
    }
}