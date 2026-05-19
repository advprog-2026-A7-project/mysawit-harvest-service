package com.mysawit.harvest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.harvest.dto.AuthenticatedUser;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.UpdateHarvestStatusRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.security.JwtIdentityProvider;
import com.mysawit.harvest.service.HarvestService;
import com.mysawit.harvest.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HarvestController.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HarvestService harvestService;

    @MockitoBean
    private JwtIdentityProvider jwtIdentityProvider;

    @MockitoBean
    private StorageService storageService;

    private UUID harvesterId;
    private UUID foremanId;
    private AuthenticatedUser mockHarvester;
    private AuthenticatedUser mockForeman;
    private LogHarvestRequest validRequest;
    private UpdateHarvestStatusRequest updateStatusRequest;
    private MockMultipartFile mockFile1;
    private MockMultipartFile mockFile2;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();

        mockHarvester = Mockito.mock(AuthenticatedUser.class);
        when(mockHarvester.id()).thenReturn(harvesterId);
        when(mockHarvester.isHarvester()).thenReturn(true);
        when(mockHarvester.isForeman()).thenReturn(false);

        mockForeman = Mockito.mock(AuthenticatedUser.class);
        when(mockForeman.id()).thenReturn(foremanId);
        when(mockForeman.isHarvester()).thenReturn(false);
        when(mockForeman.isForeman()).thenReturn(true);

        validRequest = new LogHarvestRequest();
        validRequest.setPlantationId("PLT-12345678");
        validRequest.setWeight(777.0);
        validRequest.setNews("Successful harvest");

        updateStatusRequest = new UpdateHarvestStatusRequest();

        mockFile1 = new MockMultipartFile("files", "foto1.jpg", MediaType.IMAGE_JPEG_VALUE, "bukti-panen-1".getBytes());
        mockFile2 = new MockMultipartFile("files", "foto2.jpg", MediaType.IMAGE_JPEG_VALUE, "bukti-panen-2".getBytes());
    }

    // HARVEST LOG ------------------------------------------------------------------
    @Test
    void logHarvestSuccess() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        when(storageService.uploadFiles(anyList())).thenReturn(List.of("https://supabase.co/foto1.jpg"));
        when(harvestService.logHarvest(any(), eq(harvesterId)))
                .thenReturn(HarvestResponse.builder().id(randomId).build());

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(validRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/harvests")
                        .file(mockFile1)
                        .file(mockFile2)
                        .part(requestPart)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Harvest successfully logged"))
                .andExpect(jsonPath("$.id").value(randomId.toString()));
    }

    @Test
    void alreadyLogged() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        when(storageService.uploadFiles(anyList())).thenReturn(List.of("https://supabase.co/foto1.jpg"));
        when(harvestService.logHarvest(any(), any()))
                .thenThrow(new AlreadyLoggedHarvestTodayException("Already logged today"));

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(validRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/harvests")
                        .file(mockFile1)
                        .part(requestPart)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Already logged today"));
    }

    @Test
    void validationFailed() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        LogHarvestRequest invalidRequest = new LogHarvestRequest();

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(invalidRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/harvests")
                        .file(mockFile1)
                        .part(requestPart)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void logHarvest_ThrowsIllegalArgumentException() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        when(storageService.uploadFiles(anyList())).thenReturn(List.of("https://supabase.co/foto1.jpg"));

        when(harvestService.logHarvest(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid input data"));

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(validRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/harvests")
                        .file(mockFile1)
                        .part(requestPart)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid input data"));
    }

    @Test
    void logHarvest_WithAnEmptyFileInList_ShouldThrowBadRequest() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(validRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        org.springframework.mock.web.MockPart validFile = new org.springframework.mock.web.MockPart("files", "foto1.jpg", "bukti".getBytes());
        org.springframework.mock.web.MockPart emptyFile = new org.springframework.mock.web.MockPart("files", "foto2.jpg", new byte[0]);
        validFile.getHeaders().setContentType(MediaType.IMAGE_JPEG);
        emptyFile.getHeaders().setContentType(MediaType.IMAGE_JPEG);

        mockMvc.perform(multipart("/harvests")
                        .part(validFile)
                        .part(emptyFile)
                        .part(requestPart)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("At least one valid photo file must be provided."));
    }

    @Test
    void logHarvest_WithNoFilesPart_ShouldThrowBadRequest() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);

        org.springframework.mock.web.MockPart requestPart = new org.springframework.mock.web.MockPart(
                "request", objectMapper.writeValueAsBytes(validRequest)
        );
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/harvests")
                        .part(requestPart)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("At least one valid photo file must be provided."));
    }

    // HARVESTER VIEW HARVEST ------------------------------------------------------------------
    @Test
    void getHistorySuccess() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        HarvestResponse res = HarvestResponse.builder().weight(777.0).build();

        when(harvestService.harvesterViewHarvest(any(), eq(harvesterId)))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/harvests/my")
                        .header("Authorization", "Bearer token")
                        .param("startDate", "2026-03-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weight").value(777.0));
    }

    @Test
    void getHistoryUnauthorized() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        when(harvestService.harvesterViewHarvest(any(), any()))
                .thenThrow(new UnauthorizedUserException("Unauthorized access"));

        mockMvc.perform(get("/harvests/my")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ACCESS"));
    }

    @Test
    void viewMyHistory_WhenUserIsNull_ReturnsNullId() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(null);

        when(harvestService.harvesterViewHarvest(any(), isNull()))
                .thenThrow(new UnauthorizedUserException("Harvester identity is required"));

        mockMvc.perform(get("/harvests/my")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());

        verify(jwtIdentityProvider).getAuthenticatedUser(anyString());
    }

    @Test
    void getHistoryWithStatusSuccess() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);

        mockMvc.perform(get("/harvests/my")
                        .header("Authorization", "Bearer token")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    // FOREMAN VIEW HARVEST ------------------------------------------------------------------
    @Test
    void viewAllHistoryForemanSuccess() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);
        HarvestResponse res = HarvestResponse.builder().weight(500.0).build();

        when(harvestService.foremanViewHarvest(any(), eq(foremanId)))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/harvests")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weight").value(500.0));
    }

    @Test
    void viewAllHistoryForbiddenForHarvester() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);

        when(harvestService.foremanViewHarvest(any(), isNull()))
                .thenThrow(new UnauthorizedUserException("Only registered foremen are permitted to access."));

        mockMvc.perform(get("/harvests")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ACCESS"));
    }

    @Test
    void viewAllHistoryNoIdentity() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(null);
        when(harvestService.foremanViewHarvest(any(), isNull()))
                .thenThrow(new UnauthorizedUserException("Required identity."));

        mockMvc.perform(get("/harvests")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    // FOREMAN UPDATE STATUS ------------------------------------------------------------------
    @Test
    void updateStatusSuccess() throws Exception {
        UUID harvestId = UUID.randomUUID();
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);
        when(harvestService.updateHarvestStatus(any(), eq(foremanId)))
                .thenReturn(HarvestResponse.builder().id(harvestId).status(HarvestStatus.APPROVED).build());

        mockMvc.perform(patch("/harvests/update")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateStatusForbidden() throws Exception {
        updateStatusRequest.setId(UUID.randomUUID());
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);

        when(harvestService.updateHarvestStatus(any(), isNull()))
                .thenThrow(new UnauthorizedUserException("Required foreman identity."));

        mockMvc.perform(patch("/harvests/update")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatusAlreadyProcessed() throws Exception {
        updateStatusRequest.setId(UUID.randomUUID());
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);

        when(harvestService.updateHarvestStatus(any(), eq(foremanId)))
                .thenThrow(new HarvestStatusAlreadyUpdatedException("Status already processed."));

        mockMvc.perform(patch("/harvests/update")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatusNotFound() throws Exception {
        updateStatusRequest.setId(UUID.randomUUID());
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);
        updateStatusRequest.setRejectionReason(null);

        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);

        when(harvestService.updateHarvestStatus(any(), any()))
                .thenThrow(new HarvestLogNotFoundException("Not found"));

        mockMvc.perform(patch("/harvests/update")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusIllegalArgument() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);
        when(harvestService.updateHarvestStatus(any(), any()))
                .thenThrow(new IllegalArgumentException("Rejection reason missing"));

        mockMvc.perform(patch("/harvests/update")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isBadRequest());
    }

    // GENERAL VIEW ------------------------------------------------------------------
    @Test
    void getDetail_Success() throws Exception {
        UUID harvestId = UUID.randomUUID();
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);
        when(harvestService.getHarvestDetail(eq(harvestId), any(), any()))
                .thenReturn(HarvestResponse.builder().id(harvestId).build());

        mockMvc.perform(get("/harvests/" + harvestId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(harvestId.toString()));
    }

    @Test
    void getDetail_Unauthorized() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockHarvester);
        when(harvestService.getHarvestDetail(any(), any(), any()))
                .thenThrow(new UnauthorizedUserException("Not yours"));

        mockMvc.perform(get("/harvests/" + UUID.randomUUID())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDetail_NotFound() throws Exception {
        when(jwtIdentityProvider.getAuthenticatedUser(anyString())).thenReturn(mockForeman);
        when(harvestService.getHarvestDetail(any(), any(), any()))
                .thenThrow(new HarvestLogNotFoundException("Not found"));

        mockMvc.perform(get("/harvests/" + UUID.randomUUID())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

}