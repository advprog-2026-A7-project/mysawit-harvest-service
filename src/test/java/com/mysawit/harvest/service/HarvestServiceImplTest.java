package com.mysawit.harvest.service;

import com.mysawit.harvest.adapter.PayrollAdapter;
import com.mysawit.harvest.client.IdentityClient;
import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.mapper.HarvestMapper;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.HarvestRepository;
import com.mysawit.harvest.repository.UserReplicaRepository;

import com.mysawit.harvest.service.validation.HarvestValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceImplTest {
    @Spy
    private HarvestMapper harvestMapper = new HarvestMapper();

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private IdentityClient identityClient;

    @Mock
    private UserReplicaRepository userReplicaRepository;

    @Mock
    private PayrollAdapter payrollAdapter;

    @InjectMocks
    private HarvestServiceImpl harvestService;

    @Mock
    private HarvestValidationChain harvestValidationChain;

    private UUID harvesterId;
    private UUID foremanId;
    private UUID plantationId;
    private UUID harvestId;
    private String harvesterName;

    private LogHarvestRequest logRequest;
    private HarvesterViewHarvestRequest harvesterViewRequest;
    private UpdateHarvestStatusRequest updateStatusRequest;
    private IdentityUserResponse mockUser;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = UUID.randomUUID();
        harvestId = UUID.randomUUID();
        harvesterName = "Strawberry Shortcake";

        logRequest = new LogHarvestRequest();
        logRequest.setPlantationId(plantationId);
        logRequest.setWeight(777.0);
        logRequest.setNews("Successful harvest");

        harvesterViewRequest = new HarvesterViewHarvestRequest();

        updateStatusRequest = new UpdateHarvestStatusRequest();

        mockUser = new IdentityUserResponse();
    }

    // HARVEST LOG ------------------------------------------------------------------
    private void mockValidationChain() {
        doNothing().when(harvestValidationChain).validate(logRequest, harvesterId);
    }

    @Test
    void logHarvest_Success() {
        mockValidationChain();

        mockUser.setName(harvesterName);

        when(identityClient.getUserById(harvesterId)).thenReturn(mockUser);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(harvesterName, response.getHarvesterName());
        assertEquals(foremanId, response.getForemanId());
        assertEquals(HarvestStatus.PENDING, response.getStatus());

        verify(identityClient).getUserById(harvesterId);
        verify(identityClient).getAssignedForemanId(harvesterId);
        verify(harvestRepository).save(any(Harvest.class));
    }


    @Test
    void logHarvest_ReplicaHit_SkipsIdentityClient() {
        mockValidationChain();

        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name(harvesterName)
                .role("BURUH")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId);

        assertEquals(harvesterName, response.getHarvesterName());
        assertEquals(foremanId, response.getForemanId());
        verifyNoInteractions(identityClient);
    }

    @Test
    void logHarvest_ReplicaMissing_FallsBackToIdentityClient() {
        mockValidationChain();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.empty());

        mockUser.setName(harvesterName);
        when(identityClient.getUserById(harvesterId)).thenReturn(mockUser);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId);

        assertEquals(harvesterName, response.getHarvesterName());
        assertEquals(foremanId, response.getForemanId());
        verify(identityClient).getUserById(harvesterId);
        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void logHarvest_ReplicaIncomplete_FallsBackToIdentityClient() {
        mockValidationChain();

        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name(harvesterName)
                .role("BURUH")
                .mandorId(null)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        mockUser.setName(harvesterName);
        when(identityClient.getUserById(harvesterId)).thenReturn(mockUser);
        when(identityClient.getAssignedForemanId(harvesterId)).thenReturn(foremanId);
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(inv -> inv.getArgument(0));

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId);

        assertEquals(foremanId, response.getForemanId());
        verify(identityClient).getAssignedForemanId(harvesterId);
    }

    @Test
    void logHarvest_ReplicaWrongRole_FallsBackToIdentityClient() {
        mockValidationChain();

        UserReplica replica = UserReplica.builder()
                .id(harvesterId)
                .name(harvesterName)
                .role("MANDOR")
                .mandorId(foremanId)
                .build();

        when(userReplicaRepository.findById(harvesterId)).thenReturn(Optional.of(replica));

        mockUser.setName(harvesterName);
        when(identityClient.getUserById(harvesterId)).thenReturn(mockUser);
        when(identityClient.getAssignedForemanId(harvesterId))
                .thenThrow(new UnauthorizedUserException("User is not a harvester."));

        assertThrows(UnauthorizedUserException.class,
                () -> harvestService.logHarvest(logRequest, harvesterId));
    }

    // HARVESTER VIEW ------------------------------------------------------------------
    @Test
    void harvesterViewHarvest_FilterByHarvesterId() {
        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of(
                        Harvest.builder().harvesterId(harvesterId).build()
                ));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId);

        assertEquals(1, responses.size());
        verify(harvestRepository).findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any());
    }

    @Test
    void harvesterViewHarvest_ReturnEmptyList() {
        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(harvestRepository).findAllByHarvesterIdAndDateAndStatus(any(), any(), any(), any());
    }

    @Test
    void harvesterViewHarvest_WithSpecificStatus() {
        harvesterViewRequest.setStatus(HarvestStatus.APPROVED);

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), eq(HarvestStatus.APPROVED), any(), any()))
                .thenReturn(List.of(
                        Harvest.builder().harvesterId(harvesterId).status(HarvestStatus.APPROVED).build()
                ));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId);

        assertEquals(HarvestStatus.APPROVED, responses.getFirst().getStatus());
        verify(harvestRepository).findAllByHarvesterIdAndDateAndStatus(any(), eq(HarvestStatus.APPROVED), any(), any());
    }

    // FOREMAN VIEW ------------------------------------------------------------------
    @Test
    void foremanViewHarvest_Success() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();
        req.setHarvesterName("Strawberry Shortcake");
        req.setStartDate(LocalDateTime.now().minusDays(7));
        req.setEndDate(LocalDateTime.now());

        when(harvestRepository.findAllByHarvesterNameAndDate(
                eq(foremanId), any(), any(), any()))
                .thenReturn(List.of(new Harvest(), new Harvest()));

        List<HarvestResponse> responses = harvestService.foremanViewHarvest(req, foremanId);

        assertEquals(2, responses.size());
        verify(harvestRepository).findAllByHarvesterNameAndDate(
                eq(foremanId), eq("Strawberry Shortcake"), any(), any());
    }

    // FOREMAN UPDATE STATUS ------------------------------------------------------------------
    @Test
    void updateHarvestStatus_Success_Approved_WithRabbitMQ() {
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);
        updateStatusRequest.setRejectionReason(null);

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(foremanId)
                .harvesterId(harvesterId)
                .weight(777.0)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(i -> i.getArgument(0));

        HarvestResponse response = harvestService.updateHarvestStatus(updateStatusRequest, foremanId);

        assertNotNull(response);
        assertEquals(HarvestStatus.APPROVED, response.getStatus());
        verify(harvestRepository).save(any(Harvest.class));

        verify(payrollAdapter, times(1)).publishApprovedHarvest(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_WrongForeman() {
        UUID harvestId = UUID.randomUUID();
        updateStatusRequest.setId(harvestId);

        UUID anotherForemanId = UUID.randomUUID();

        when(harvestRepository.findById(eq(harvestId))).thenReturn(Optional.of(
                Harvest.builder()
                        .id(harvestId)
                        .foremanId(anotherForemanId)
                        .status(HarvestStatus.PENDING)
                        .build()
        ));

        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateHarvestStatus_RejectedWithRemarks() {
        UUID harvestId = UUID.randomUUID();

        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.REJECTED);
        updateStatusRequest.setRejectionReason("Bad harvest.");

        Harvest existingHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(foremanId)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(java.util.Optional.of(existingHarvest));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HarvestResponse response = harvestService.updateHarvestStatus(updateStatusRequest, foremanId);

        assertNotNull(response);
        assertEquals(HarvestStatus.REJECTED, response.getStatus());
        assertEquals("Bad harvest.", response.getRejectionReason());

        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_NotFound() {
        UUID randomId = UUID.randomUUID();
        updateStatusRequest.setId(randomId);

        when(harvestRepository.findById(eq(randomId))).thenReturn(java.util.Optional.empty());

        assertThrows(HarvestLogNotFoundException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateHarvestStatus_Rejected_ShouldNotSendRabbitMQMessage() {
        UpdateHarvestStatusRequest request = new UpdateHarvestStatusRequest();
        request.setId(harvestId);
        request.setStatus(HarvestStatus.REJECTED);
        request.setRejectionReason("Bad harvest");

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(foremanId)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));
        when(harvestRepository.save(any(Harvest.class))).thenReturn(mockHarvest);

        harvestService.updateHarvestStatus(request, foremanId);

        verify(payrollAdapter, never()).publishApprovedHarvest(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_InvalidTargetStatus_ThrowsException() {
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.PENDING);

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(foremanId)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        assertEquals("Harvest status can only be updated to APPROVED or REJECTED status.", exception.getMessage());

        verify(harvestRepository, never()).save(any(Harvest.class));
        verify(payrollAdapter, never()).publishApprovedHarvest(any(Harvest.class));
    }

    // GENERAL VIEW ------------------------------------------------------------------
    @Test
    void getHarvestDetail_SuccessAsForeman() {
        UUID harvestId = UUID.randomUUID();

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .harvesterId(UUID.randomUUID())
                .foremanId(foremanId)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        HarvestResponse response = harvestService.getHarvestDetail(harvestId, null, foremanId);

        assertNotNull(response);
        assertEquals(harvestId, response.getId());
        verify(harvestRepository).findById(harvestId);
    }

    @Test
    void getHarvestDetail_SuccessAsOwner() {
        UUID harvestId = UUID.randomUUID();
        UUID myId = UUID.randomUUID();
        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .harvesterId(myId)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        HarvestResponse response = harvestService.getHarvestDetail(harvestId, myId, null);

        assertNotNull(response);
        assertEquals(harvestId, response.getId());
    }

    @Test
    void getHarvestDetail_ForbiddenForOtherHarvester() {
        UUID harvestId = UUID.randomUUID();
        UUID myId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .harvesterId(otherId)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.getHarvestDetail(harvestId, myId, null));
    }

    @Test
    void getHarvestDetail_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(harvestRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(HarvestLogNotFoundException.class, () ->
                harvestService.getHarvestDetail(randomId, null, foremanId));
    }

    @Test
    void getHarvestDetail_ForbiddenForOtherForeman() {
        UUID harvestId = UUID.randomUUID();
        UUID actualForemanId = UUID.randomUUID();
        UUID intruderForemanId = UUID.randomUUID();

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(actualForemanId)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.getHarvestDetail(harvestId, null, intruderForemanId));
    }
}