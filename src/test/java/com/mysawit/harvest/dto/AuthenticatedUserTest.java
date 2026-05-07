package com.mysawit.harvest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticatedUserTest {

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Should return true for isHarvester when role is BURUH")
    void testIsHarvester_True() {
        AuthenticatedUser user = new AuthenticatedUser(userId, "BURUH");

        assertTrue(user.isHarvester(), "User with BURUH role should be identified as harvester");
        assertFalse(user.isForeman(), "User with BURUH role should not be identified as foreman");
    }

    @Test
    @DisplayName("Should return true for isForeman when role is MANDOR")
    void testIsForeman_True() {
        AuthenticatedUser user = new AuthenticatedUser(userId, "MANDOR");

        assertTrue(user.isForeman(), "User with MANDOR role should be identified as foreman");
        assertFalse(user.isHarvester(), "User with MANDOR role should not be identified as harvester");
    }

    @Test
    @DisplayName("Should return false for both when role is unknown or ADMIN")
    void testUnknownRole() {
        AuthenticatedUser user = new AuthenticatedUser(userId, "ADMIN");

        assertFalse(user.isHarvester());
        assertFalse(user.isForeman());
    }

    @Test
    @DisplayName("Should handle null role gracefully")
    void testNullRole() {
        AuthenticatedUser user = new AuthenticatedUser(userId, null);

        assertDoesNotThrow(() -> {
            assertFalse(user.isHarvester());
            assertFalse(user.isForeman());
        }, "Logic should use .equals() in a way that prevents NullPointerException");
    }

    @Test
    @DisplayName("Record properties should be correctly assigned")
    void testRecordProperties() {
        AuthenticatedUser user = new AuthenticatedUser(userId, "BURUH");

        assertEquals(userId, user.id());
        assertEquals("BURUH", user.role());
    }
}