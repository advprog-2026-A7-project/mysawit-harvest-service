package com.mysawit.harvest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.service.HarvestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HarvestController.class)
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HarvestService harvestService;

    private UUID harvesterId;
    private UUID foremanId;
    private LogHarvestRequest validRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();

        validRequest = new LogHarvestRequest();
        validRequest.setPlantationId(UUID.randomUUID());
        validRequest.setWeight(300.5);
        validRequest.setNews("Successful harvest");
    }

    @Test
    void logHarvestSuccess() throws Exception {
        HarvestResponse response = HarvestResponse.builder()
                .harvesterId(harvesterId)
                .foremanId(foremanId)
                .status(HarvestStatus.PENDING)
                .weight(300.5)
                .build();

        when(harvestService.logHarvest(any(LogHarvestRequest.class), any(UUID.class), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(post("/harvests")
                        .header("X-Harvester-Id", harvesterId)
                        .header("X-Foreman-Id", foremanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void alreadyLogged() throws Exception {
        when(harvestService.logHarvest(any(), any(), any()))
                .thenThrow(new AlreadyLoggedHarvestTodayException("Already logged today"));

        mockMvc.perform(post("/harvests")
                        .header("X-Harvester-Id", harvesterId)
                        .header("X-Foreman-Id", foremanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ALREADY_LOGGED_TODAY"));
    }

    @Test
    void validationFailed() throws Exception {
        LogHarvestRequest invalidRequest = new LogHarvestRequest();

        mockMvc.perform(post("/harvests")
                        .header("X-Harvester-Id", harvesterId)
                        .header("X-Foreman-Id", foremanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void getHistorySuccess() throws Exception {
        HarvestResponse res = HarvestResponse.builder()
                .harvesterId(harvesterId)
                .weight(300.5)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestService.viewHarvest(any(), eq(harvesterId), any()))
                .thenReturn(java.util.List.of(res));

        mockMvc.perform(get("/harvests/my")
                        .header("X-Harvester-Id", harvesterId)
                        .param("startDate", "2026-03-01T00:00:00")
                        .param("endDate", "2026-03-07T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].weight").value(300.5));
    }

    @Test
    void getHistoryUnauthorized() throws Exception {
        when(harvestService.viewHarvest(any(), any(), any()))
                .thenThrow(new com.mysawit.harvest.exception.UnauthorizedUserException("Unauthorized"));

        mockMvc.perform(get("/harvests/my"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ACCESS"));
    }
}