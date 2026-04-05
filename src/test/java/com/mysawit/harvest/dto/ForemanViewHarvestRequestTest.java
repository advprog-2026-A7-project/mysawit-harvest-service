package com.mysawit.harvest.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ForemanViewHarvestRequestTest {

    private ForemanViewHarvestRequest request;

    @BeforeEach
    void setUp() {
        request = new ForemanViewHarvestRequest();
    }

    @Test
    void setHarvesterName() {
        request.setHarvesterName("Strawberry Shortcake");
        assertEquals("Strawberry Shortcake", request.getHarvesterName());
    }

    @Test
    void setStartDate_ShouldNormalizeToStartOfDay() {
        LocalDateTime middleOfDay = LocalDateTime.of(2026, 4, 5, 14, 30);
        request.setStartDate(middleOfDay);

        assertEquals(LocalTime.MIN, request.getStartDate().toLocalTime());
        assertEquals(5, request.getStartDate().getDayOfMonth());
    }

    @Test
    void setEndDate_ShouldNormalizeToEndOfDay() {
        LocalDateTime middleOfDay = LocalDateTime.of(2026, 4, 5, 14, 30);
        request.setEndDate(middleOfDay);

        assertEquals(LocalTime.MAX, request.getEndDate().toLocalTime());
    }

    @Test
    void nullDates_ShouldBeHandled() {
        request.setStartDate(null);
        request.setEndDate(null);

        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }
}