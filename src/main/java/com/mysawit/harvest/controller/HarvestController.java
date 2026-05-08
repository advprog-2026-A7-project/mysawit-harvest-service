package com.mysawit.harvest.controller;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.security.JwtIdentityProvider;
import com.mysawit.harvest.service.HarvestService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/harvests")
@RequiredArgsConstructor
public class HarvestController {
    private final HarvestService harvestService;
    private final JwtIdentityProvider jwtIdentityProvider;

    @PostMapping
    public ResponseEntity<Map<String, Object>> logHarvest(
            @Valid @RequestBody LogHarvestRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        AuthenticatedUser user = jwtIdentityProvider.getAuthenticatedUser(authorization);

        HarvestResponse response = harvestService.logHarvest(request, getHarvesterId(user)
        );

        Map<String, Object> responseBody = Map.of(
                "message", "Harvest successfully logged",
                "id", response.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping("/my")
    public ResponseEntity<List<HarvestResponse>> viewMyHistory(
            @ModelAttribute HarvesterViewHarvestRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        AuthenticatedUser user = jwtIdentityProvider.getAuthenticatedUser(authorization);

        return ResponseEntity.ok(harvestService.harvesterViewHarvest(request, getHarvesterId(user))
        );
    }

    @GetMapping
    public ResponseEntity<List<HarvestResponse>> viewAllHistory(
            @ModelAttribute ForemanViewHarvestRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        AuthenticatedUser user = jwtIdentityProvider.getAuthenticatedUser(authorization);

        List<HarvestResponse> responses = harvestService.foremanViewHarvest(request, getForemanId(user));

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HarvestResponse> getHarvestDetail(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {
        AuthenticatedUser user = jwtIdentityProvider.getAuthenticatedUser(authorization);

        HarvestResponse response = harvestService.getHarvestDetail(id, getHarvesterId(user), getForemanId(user)
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<HarvestResponse> updateStatus(
            @Valid @RequestBody UpdateHarvestStatusRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        AuthenticatedUser user = jwtIdentityProvider.getAuthenticatedUser(authorization);

        HarvestResponse response = harvestService.updateHarvestStatus(request, getForemanId(user)
        );

        return ResponseEntity.ok(response);
    }

    private UUID getHarvesterId(AuthenticatedUser user) {
        if (user == null) return null;
        return user.isHarvester() ? user.id() : null;
    }

    private UUID getForemanId(AuthenticatedUser user) {
        if (user == null) return null;
        return user.isForeman() ? user.id() : null;
    }
}