package com.mysawit.harvest.service.harvester;

import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReplicaFirstHarvesterContextResolver implements HarvesterContextResolver {
    private static final String HARVESTER_ROLE = "BURUH";

    private final UserReplicaRepository userReplicaRepository;

    @Override
    public HarvesterContext resolve(UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId)
                .orElseThrow(() -> new UnauthorizedUserException("Harvester registration data not found."));

        if (!HARVESTER_ROLE.equals(replica.getRole())) {
            throw new UnauthorizedUserException("User is not a harvester.");
        }

        if (replica.getMandorId() == null) {
            throw new UnauthorizedUserException("Harvester is not assigned to any foreman.");
        }

        return new HarvesterContext(replica.getName(), replica.getMandorId());
    }
}
