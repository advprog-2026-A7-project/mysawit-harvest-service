package com.mysawit.harvest.client;

import com.mysawit.harvest.dto.IdentityUserResponse;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityClient {
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String HARVESTER_ROLE = "BURUH";

    private final RestClient.Builder restClientBuilder;

    @Value("${services.identity.url}")
    private String identityServiceUrl;

    @Value("${services.identity.internal-api-key}")
    private String internalApiKey;

    public IdentityUserResponse getUserById(UUID userId) {
        try {
            IdentityUserResponse response = restClientBuilder
                    .baseUrl(identityServiceUrl)
                    .build()
                    .get()
                    .uri("/api/internal/users/{userId}", userId)
                    .header(INTERNAL_API_KEY_HEADER, internalApiKey)
                    .retrieve()
                    .body(IdentityUserResponse.class);

            if (response == null) {
                throw new UnauthorizedUserException("User identity could not be resolved.");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new UnauthorizedUserException("User identity could not be resolved.");
        }
    }

    public UUID getAssignedForemanId(UUID harvesterId) {
        IdentityUserResponse user = getUserById(harvesterId);

        if (!HARVESTER_ROLE.equals(user.getRole())) {
            throw new UnauthorizedUserException("User is not a harvester.");
        }

        if (user.getMandorId() == null || user.getMandorId().isBlank()) {
            throw new UnauthorizedUserException("Harvester is not assigned to any foreman.");
        }

        return UUID.fromString(user.getMandorId());
    }
}
