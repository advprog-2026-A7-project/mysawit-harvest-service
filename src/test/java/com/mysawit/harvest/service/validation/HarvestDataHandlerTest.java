package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HarvestDataHandlerTest {

    private HarvestDataHandler handler;
    private LogHarvestRequest request;
    private UUID harvesterId;

    @BeforeEach
    void setUp() {
        handler = new HarvestDataHandler();
        harvesterId = UUID.randomUUID();

        request = new LogHarvestRequest();
        request.setWeight(777.0);
        request.setNews("Panennya bagus");
        request.setPhotos(List.of("photo.jpg"));
    }

    @Test
    void handle_passes_whenDataIsValid() {
        assertDoesNotThrow(() -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenWeightIsNull() {
        request.setWeight(null);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenWeightIsZero() {
        request.setWeight(0.0);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenWeightIsNegative() {
        request.setWeight(-1.0);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenPhotosEmpty() {
        request.setPhotos(List.of());

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenPhotosIsNull() {
        request.setPhotos(null);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_passes_whenPhotosContainMoreThanOnePhoto() {
        request.setPhotos(List.of("photo-1.jpg", "photo-2.jpg", "photo-3.jpg"));

        assertDoesNotThrow(() -> handler.handle(request, harvesterId));
    }



    @Test
    void handle_throwsException_whenNewsBlank() {
        request.setNews("   ");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

    @Test
    void handle_throwsException_whenNewsIsNull() {
        request.setNews(null);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(request, harvesterId));
    }

}
