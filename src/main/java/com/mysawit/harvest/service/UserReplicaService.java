package com.mysawit.harvest.service;

import com.mysawit.harvest.event.UserAssignedEvent;
import com.mysawit.harvest.event.UserDeletedEvent;
import com.mysawit.harvest.event.UserRegisteredEvent;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserReplicaService {

    private static final Logger log = LoggerFactory.getLogger(UserReplicaService.class);

    private final UserReplicaRepository repository;

    @Transactional
    public void upsertFromRegistration(UserRegisteredEvent event) {
        UUID userId = parseUserId(event.getUserId());
        if (userId == null) {
            log.warn("Skipping user.registered with invalid userId={}", event.getUserId());
            return;
        }

        String resolvedName = (event.getUsername() != null && !event.getUsername().isBlank())
                ? event.getUsername()
                : event.getEmail();

        UserReplica replica = repository.findById(userId).orElseGet(() ->
                UserReplica.builder().id(userId).build());

        replica.setName(resolvedName);
        replica.setRole(event.getRole());

        repository.save(replica);
        log.info("UserReplica upserted from user.registered userId={} role={}", userId, event.getRole());
    }

    @Transactional
    public void applyAssignment(UserAssignedEvent event) {
        UUID userId = parseUserId(event.getUserId());
        if (userId == null) {
            log.warn("Skipping user.assigned with invalid userId={}", event.getUserId());
            return;
        }

        UUID mandorId = parseUserId(event.getMandorId());

        UserReplica replica = repository.findById(userId).orElseGet(() ->
                UserReplica.builder().id(userId).build());

        replica.setMandorId(mandorId);

        repository.save(replica);
        log.info("UserReplica assignment applied userId={} mandorId={} action={}",
                userId, mandorId, event.getAction());
    }

    @Transactional
    public void deleteUser(UserDeletedEvent event) {
        UUID userId = parseUserId(event.getUserId());
        if (userId == null) {
            log.warn("Skipping user.deleted with invalid userId={}", event.getUserId());
            return;
        }

        if (repository.existsById(userId)) {
            repository.deleteById(userId);
            log.info("UserReplica deleted userId={} role={}", userId, event.getRole());
        } else {
            log.warn("UserReplica not found for deletion userId={}", userId);
        }
    }

    private UUID parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
