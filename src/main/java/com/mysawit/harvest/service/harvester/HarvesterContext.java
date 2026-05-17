package com.mysawit.harvest.service.harvester;

import java.util.UUID;

public record HarvesterContext(String harvesterName, UUID foremanId) {
}
