package com.mysawit.harvest.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ForemanViewHarvestRequestTest {
    @Test
    void testGettersAndSetters() {
        ForemanViewHarvestRequest request = new ForemanViewHarvestRequest();
        LocalDate testDate = LocalDate.of(2026, 5, 21);

        request.setHarvesterName("Strawberry");
        request.setDate(testDate);

        assertEquals("Strawberry", request.getHarvesterName());
        assertEquals(testDate, request.getDate());
    }

    @Test
    void testNullValues() {
        ForemanViewHarvestRequest request = new ForemanViewHarvestRequest();
        request.setDate(null);
        request.setHarvesterName(null);

        assertNull(request.getDate());
        assertNull(request.getHarvesterName());
    }
}