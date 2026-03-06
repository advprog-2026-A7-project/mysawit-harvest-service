package com.mysawit.harvest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.harvest.dto.HarvestRequest;
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
import static org.mockito.Mockito.when;
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
    private HarvestRequest validRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();

        validRequest = new HarvestRequest();
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

        when(harvestService.logHarvest(any(HarvestRequest.class), any(UUID.class), any(UUID.class)))
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
        HarvestRequest invalidRequest = new HarvestRequest();

        mockMvc.perform(post("/harvests")
                        .header("X-Harvester-Id", harvesterId)
                        .header("X-Foreman-Id", foremanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}