package com.mysawit.harvest.mapper;

import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarvestMapperTest {
    private HarvestMapper harvestMapper;

    @BeforeEach
    void setUp() {
        harvestMapper = new HarvestMapper();
    }

    @Test
    void mapToResponse_shouldMapAllHarvestFields() {
        UUID harvestId = UUID.randomUUID();
        UUID plantationId = UUID.randomUUID();
        UUID harvesterId = UUID.randomUUID();
        UUID foremanId = UUID.randomUUID();
        LocalDateTime harvestDate = LocalDateTime.now().minusDays(1);
        LocalDateTime statusUpdatedDate = LocalDateTime.now();

        Harvest harvest = Harvest.builder()
                .id(harvestId)
                .plantationId(plantationId)
                .harvesterId(harvesterId)
                .foremanId(foremanId)
                .harvesterName("Strawberry Shortcake")
                .weight(777.0)
                .news("Panen hari ini mantap")
                .photos(List.of("photo-1.jpg", "photo-2.jpg"))
                .status(HarvestStatus.REJECTED)
                .rejectionReason("Foto buram")
                .harvestDate(harvestDate)
                .statusUpdatedDate(statusUpdatedDate)
                .build();

        HarvestResponse response = harvestMapper.mapToResponse(harvest);

        assertEquals(harvestId, response.getId());
        assertEquals(plantationId, response.getPlantationId());
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(foremanId, response.getForemanId());
        assertEquals("Strawberry Shortcake", response.getHarvesterName());
        assertEquals(777.0, response.getWeight());
        assertEquals("Panen hari ini mantap", response.getNews());
        assertEquals(List.of("photo-1.jpg", "photo-2.jpg"), response.getPhotos());
        assertEquals(HarvestStatus.REJECTED, response.getStatus());
        assertEquals("Foto buram", response.getRejectionReason());
        assertEquals(harvestDate, response.getHarvestDate());
        assertEquals(statusUpdatedDate, response.getStatusUpdatedDate());
    }
}
