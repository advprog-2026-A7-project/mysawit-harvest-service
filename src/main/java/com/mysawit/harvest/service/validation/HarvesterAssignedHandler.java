package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HarvesterAssignedHandler extends HarvestValidationHandler {
    private static final String HARVESTER_ROLE = "BURUH";

    private final UserReplicaRepository userReplicaRepository;

    @Override
    protected void validate(LogHarvestRequest request, UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId)
                .orElseThrow(() -> new UnauthorizedUserException("Harvester registration data not found in local replica."));

        if (!HARVESTER_ROLE.equals(replica.getRole())) {
            throw new UnauthorizedUserException("User is not a harvester.");
        }

        if (replica.getMandorId() == null) {
            throw new UnauthorizedUserException("Harvester is not assigned to any foreman.");
        }
    }
}
