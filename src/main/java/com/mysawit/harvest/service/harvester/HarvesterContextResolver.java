package com.mysawit.harvest.service.harvester;

import java.util.UUID;

public interface HarvesterContextResolver {
    HarvesterContext resolve(UUID harvesterId);
}