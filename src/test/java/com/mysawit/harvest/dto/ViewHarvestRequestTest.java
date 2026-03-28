package com.mysawit.harvest.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ViewHarvestRequestTest {

    private ViewHarvestRequest request;

    @BeforeEach
    void setUp() {
        request = new ViewHarvestRequest();
    }

    @Test
    void setStartDate_ShouldSetToMinTime() {
        LocalDateTime input = LocalDateTime.of(2026, 3, 20, 15, 30, 45);

        request.setStartDate(input);

        assertNotNull(request.getStartDate());
        assertEquals(20, request.getStartDate().getDayOfMonth());
        assertEquals(LocalTime.MIN, request.getStartDate().toLocalTime());

        assertEquals(0, request.getStartDate().getHour());
        assertEquals(0, request.getStartDate().getMinute());
    }

    @Test
    void setEndDate_ShouldSetToMaxTime() {
        LocalDateTime input = LocalDateTime.of(2026, 3, 20, 8, 0, 0);

        request.setEndDate(input);

        assertNotNull(request.getEndDate());
        assertEquals(20, request.getEndDate().getDayOfMonth());

        assertEquals(23, request.getEndDate().getHour());
        assertEquals(59, request.getEndDate().getMinute());
        assertEquals(59, request.getEndDate().getSecond());
    }

    @Test
    void setters_ShouldHandleNull() {
        assertDoesNotThrow(() -> {
            request.setStartDate(null);
            request.setEndDate(null);
        });

        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }

    @Test
    void singleDayRange_ShouldCoverEntireDay() {
        LocalDateTime sameDay = LocalDateTime.of(2026, 3, 1, 12, 0);

        request.setStartDate(sameDay);
        request.setEndDate(sameDay);

        assertTrue(request.getStartDate().isBefore(request.getEndDate()));
        assertEquals(LocalTime.MIN, request.getStartDate().toLocalTime());
        assertEquals(LocalTime.MAX, request.getEndDate().toLocalTime());
    }
}