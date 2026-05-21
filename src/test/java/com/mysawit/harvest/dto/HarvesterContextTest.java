package com.mysawit.harvest.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HarvesterContextTest {

    @Test
    void constructor_ShouldStoreValuesCorrectly() {
        UUID foremanId = UUID.randomUUID();
        String name = "Strawberry";

        HarvesterContext context = new HarvesterContext(name, foremanId);

        assertEquals(name, context.harvesterName());
        assertEquals(foremanId, context.foremanId());
    }

    @Test
    void equalsAndHashCode_ShouldBeTrue_WhenValuesAreIdentical() {
        UUID foremanId = UUID.randomUUID();

        HarvesterContext context1 = new HarvesterContext("Strawberry", foremanId);
        HarvesterContext context2 = new HarvesterContext("Strawberry", foremanId);

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void toString_ShouldContainAllFields() {
        UUID foremanId = UUID.randomUUID();
        HarvesterContext context = new HarvesterContext("Strawberry", foremanId);

        String toStringResult = context.toString();

        assertTrue(toStringResult.contains("Strawberry"));
        assertTrue(toStringResult.contains(foremanId.toString()));
    }
}