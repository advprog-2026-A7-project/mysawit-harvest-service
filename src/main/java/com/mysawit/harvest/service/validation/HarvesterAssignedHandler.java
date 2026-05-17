package com.mysawit.harvest.service.validation;

import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.LogHarvestRequest;
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
    private final IdentityClient identityClient;

    @Override
    protected void validate(LogHarvestRequest request, UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId).orElse(null);

        if (replica != null
                && HARVESTER_ROLE.equals(replica.getRole())
                && replica.getMandorId() != null) {
            return;
        }

        identityClient.getAssignedForemanId(harvesterId);
    }
}
