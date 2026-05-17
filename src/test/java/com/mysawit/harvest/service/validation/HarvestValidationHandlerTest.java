package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HarvestValidationHandlerTest {

    @Test
    void setNext_returnsNextHandler() {
        TestHandler first = new TestHandler();
        TestHandler second = new TestHandler();

        HarvestValidationHandler result = first.setNext(second);

        assertSame(second, result);
    }

    @Test
    void handle_callsCurrentHandlerValidation() {
        TestHandler handler = new TestHandler();
        LogHarvestRequest request = new LogHarvestRequest();
        UUID harvesterId = UUID.randomUUID();

        handler.handle(request, harvesterId);

        assertTrue(handler.wasCalled);
        assertSame(request, handler.receivedRequest);
        assertEquals(harvesterId, handler.receivedHarvesterId);
    }

    @Test
    void handle_callsNextHandler_whenNextExists() {
        TestHandler first = new TestHandler();
        TestHandler second = new TestHandler();
        LogHarvestRequest request = new LogHarvestRequest();
        UUID harvesterId = UUID.randomUUID();

        first.setNext(second);

        first.handle(request, harvesterId);

        assertTrue(first.wasCalled);
        assertTrue(second.wasCalled);
        assertSame(request, second.receivedRequest);
        assertEquals(harvesterId, second.receivedHarvesterId);
    }

    @Test
    void handle_stopsChain_whenCurrentHandlerThrowsException() {
        TestHandler first = new TestHandler(true);
        TestHandler second = new TestHandler();
        LogHarvestRequest request = new LogHarvestRequest();

        first.setNext(second);

        assertThrows(IllegalArgumentException.class,
                () -> first.handle(request, UUID.randomUUID()));

        assertTrue(first.wasCalled);
        assertFalse(second.wasCalled);
    }

    private static class TestHandler extends HarvestValidationHandler {
        private boolean wasCalled;
        private boolean shouldThrow;
        private LogHarvestRequest receivedRequest;
        private UUID receivedHarvesterId;

        private TestHandler() {
            this(false);
        }

        private TestHandler(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        protected void validate(LogHarvestRequest request, UUID harvesterId) {
            wasCalled = true;
            receivedRequest = request;
            receivedHarvesterId = harvesterId;

            if (shouldThrow) {
                throw new IllegalArgumentException("Validation failed.");
            }
        }
    }
}
