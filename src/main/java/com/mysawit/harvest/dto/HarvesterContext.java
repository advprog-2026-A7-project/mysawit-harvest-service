package com.mysawit.harvest.dto;

import java.util.UUID;

public record HarvesterContext(String harvesterName, UUID foremanId) {
}
