package com.mysawit.harvest.dto;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String role) {
    public boolean isHarvester() {
        return "BURUH".equals(role);
    }

    public boolean isForeman() {
        return "MANDOR".equals(role);
    }
}
