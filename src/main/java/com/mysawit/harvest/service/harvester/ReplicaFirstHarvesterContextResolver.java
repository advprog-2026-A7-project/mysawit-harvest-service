package com.mysawit.harvest.service.harvester;

import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.IdentityUserResponse;
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
    private final IdentityClient identityClient;

    @Override
    public HarvesterContext resolve(UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId).orElse(null);

        if (isCompleteHarvesterReplica(replica)) {
            return new HarvesterContext(replica.getName(), replica.getMandorId());
        }

        return resolveFromIdentity(harvesterId);
    }

    private boolean isCompleteHarvesterReplica(UserReplica replica) {
        return replica != null
                && HARVESTER_ROLE.equals(replica.getRole())
                && replica.getMandorId() != null
                && replica.getName() != null;
    }

    private HarvesterContext resolveFromIdentity(UUID harvesterId) {
        IdentityUserResponse harvester = identityClient.getUserById(harvesterId);
        UUID foremanId = identityClient.getAssignedForemanId(harvesterId);

        return new HarvesterContext(harvester.getName(), foremanId);
    }
}
