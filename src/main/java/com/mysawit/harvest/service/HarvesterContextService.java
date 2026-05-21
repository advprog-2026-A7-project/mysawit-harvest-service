package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.HarvesterContext;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HarvesterContextService {
    private static final String HARVESTER_ROLE = "BURUH";
    private final UserReplicaRepository userReplicaRepository;

    public HarvesterContext resolve(UUID harvesterId) {
        UserReplica replica = userReplicaRepository.findById(harvesterId)
                .orElseThrow(() -> new UnauthorizedUserException("Data buruh tidak ditemukan."));

        if (!HARVESTER_ROLE.equals(replica.getRole()) || replica.getMandorId() == null || replica.getName() == null) {
            throw new UnauthorizedUserException("Profil buruh tidak valid atau belum lengkap.");
        }

        return new HarvesterContext(replica.getName(), replica.getMandorId());
    }
}