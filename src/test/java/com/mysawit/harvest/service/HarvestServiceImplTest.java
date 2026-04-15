package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.ForemanViewHarvestRequest;
import com.mysawit.harvest.dto.LogHarvestRequest;
import com.mysawit.harvest.dto.HarvestResponse;
import com.mysawit.harvest.dto.HarvesterViewHarvestRequest;
import com.mysawit.harvest.dto.UpdateHarvestStatusRequest;
import com.mysawit.harvest.exception.AlreadyLoggedHarvestTodayException;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.HarvestStatusAlreadyUpdatedException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.repository.HarvestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    @Mock
    private HarvestRepository harvestRepository;

    @InjectMocks
    private HarvestServiceImpl harvestService;

    private UUID harvesterId;
    private UUID foremanId;
    private UUID plantationId;
    private String harvesterName;

    private LogHarvestRequest logRequest;
    private HarvesterViewHarvestRequest harvesterViewRequest;
    private UpdateHarvestStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = UUID.randomUUID();
        harvesterName = "Strawberry Shortcake";

        logRequest = new LogHarvestRequest();
        logRequest.setPlantationId(plantationId);
        logRequest.setWeight(777.0);
        logRequest.setNews("Successful harvest");

        harvesterViewRequest = new HarvesterViewHarvestRequest();

        updateStatusRequest = new UpdateHarvestStatusRequest();
    }

    // HARVEST LOG ------------------------------------------------------------------
    @Test
    void logHarvest_Success() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(false);
        when(harvestRepository.save(any(Harvest.class))).thenReturn(
                Harvest.builder()
                        .id(UUID.randomUUID())
                        .harvesterId(harvesterId)
                        .foremanId(foremanId)
                        .harvesterName(harvesterName)
                        .plantationId(plantationId)
                        .weight(777.0)
                        .news("Successful harvest")
                        .harvestDate(LocalDateTime.now())
                        .status(HarvestStatus.PENDING)
                        .build()
        );

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId, foremanId, harvesterName);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(HarvestStatus.PENDING, response.getStatus());
        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    void logHarvest_AlreadyLoggedToday_ThrowsException() {
        when(harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(
                eq(harvesterId), any(), any())).thenReturn(true);

        assertThrows(AlreadyLoggedHarvestTodayException.class, () ->
                harvestService.logHarvest(logRequest, harvesterId, foremanId, harvesterName));

        verify(harvestRepository, never()).save(any());
    }

    // HARVESTER VIEW ------------------------------------------------------------------
    @Test
    void harvesterViewHarvest_FilterByHarvesterId() {
        when(harvestRepository.findAllByHarvesterIdAndDate(eq(harvesterId), any(), any()))
                .thenReturn(List.of(
                        Harvest.builder().harvesterId(harvesterId).build()
                ));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        assertEquals(1, responses.size());
        assertEquals(harvesterId, responses.getFirst().getHarvesterId());

        verify(harvestRepository).findAllByHarvesterIdAndDate(eq(harvesterId), any(), any());
    }

    @Test
    void harvesterViewHarvest_EnsureCorrectMapping() {
        Harvest mockHarvest = Harvest.builder()
                .id(UUID.randomUUID())
                .harvesterId(harvesterId)
                .harvesterName("Strawberry Shortcake")
                .weight(777.0)
                .status(HarvestStatus.APPROVED)
                .news("Harvest from blok A")
                .build();

        when(harvestRepository.findAllByHarvesterIdAndDate(any(), any(), any()))
                .thenReturn(List.of(mockHarvest));

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        HarvestResponse result = responses.getFirst();
        assertEquals(777.0, result.getWeight());
        assertEquals(HarvestStatus.APPROVED, result.getStatus());
        assertEquals("Harvest from blok A", result.getNews());
        assertEquals("Strawberry Shortcake", result.getHarvesterName());
    }

    @Test
    void harvesterViewHarvest_ReturnEmptyList() {
        when(harvestRepository.findAllByHarvesterIdAndDate(any(), any(), any()))
                .thenReturn(List.of());

        List<HarvestResponse> responses = harvestService.harvesterViewHarvest(harvesterViewRequest, harvesterId, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void harvesterViewHarvest_AsForeman() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.harvesterViewHarvest(harvesterViewRequest, null, foremanId)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndDate(any(), any(), any());
    }

    @Test
    void harvesterViewHarvest_NoIdentity() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.harvesterViewHarvest(harvesterViewRequest, null, null)
        );

        verify(harvestRepository, never()).findAllByHarvesterIdAndDate(any(), any(), any());
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

        List<HarvestResponse> responses = harvestService.foremanViewHarvest(req, null, foremanId);

        assertEquals(2, responses.size());
        verify(harvestRepository).findAllByHarvesterNameAndDate(
                eq(foremanId), eq("Strawberry Shortcake"), any(), any());
    }

    @Test
    void foremanViewHarvest_AsHarvester() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, harvesterId, null));

        assertEquals("Only registered foremen are permitted to access.", exception.getMessage());
        verify(harvestRepository, never()).findAllByHarvesterNameAndDate(any(), any(), any(), any());
    }

    @Test
    void foremanViewHarvest_NoIdentity() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, null, null));

        assertEquals("Required identity to view harvest logs.", exception.getMessage());
        verify(harvestRepository, never()).findAllByHarvesterNameAndDate(any(), any(), any(), any());
    }

    // FOREMAN UPDATE STATUS ------------------------------------------------------------------
    @Test
    void updateHarvestStatus_Success() {
        UUID harvestId = UUID.randomUUID();

        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);

        when(harvestRepository.findById(eq(harvestId))).thenReturn(java.util.Optional.of(
                Harvest.builder()
                        .id(harvestId)
                        .foremanId(foremanId)
                        .status(HarvestStatus.PENDING)
                        .build()
        ));

        when(harvestRepository.save(any(Harvest.class))).thenAnswer(i -> i.getArguments()[0]);

        HarvestResponse response = harvestService.updateHarvestStatus(updateStatusRequest, foremanId);

        assertNotNull(response);
        assertEquals(HarvestStatus.APPROVED, response.getStatus());
        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_AsHarvester() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, null));

        verify(harvestRepository, never()).findById(any());
        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateHarvestStatus_WrongForeman() {
        UUID harvestId = UUID.randomUUID();

        updateStatusRequest.setId(harvestId);

        when(harvestRepository.findById(eq(harvestId))).thenReturn(java.util.Optional.of(
                Harvest.builder()
                        .id(harvestId)
                        .foremanId(UUID.randomUUID())
                        .build()
        ));

        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateHarvestStatus_NoIdentity() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, null));

        verify(harvestRepository, never()).findById(any());
    }

    @Test
    void updateHarvestStatus_AlreadyProcessed() {
        UUID harvestId = UUID.randomUUID();

        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.REJECTED);
        updateStatusRequest.setRejectionReason("Bad harvest.");

        when(harvestRepository.findById(eq(harvestId))).thenReturn(java.util.Optional.of(
                Harvest.builder()
                        .id(harvestId)
                        .foremanId(foremanId)
                        .status(HarvestStatus.APPROVED)
                        .build()
        ));

        assertThrows(HarvestStatusAlreadyUpdatedException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any(Harvest.class));
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
    void updateHarvestStatus_RejectedWithoutReason() {
        UUID harvestId = UUID.randomUUID();
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.REJECTED);
        updateStatusRequest.setRejectionReason(null);

        when(harvestRepository.findById(eq(harvestId))).thenReturn(java.util.Optional.of(
                Harvest.builder().id(harvestId).foremanId(foremanId).status(HarvestStatus.PENDING).build()
        ));

        assertThrows(IllegalArgumentException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));
    }

    @Test
    void updateHarvestStatus_RejectedWithBlankReason() {
        UUID harvestId = UUID.randomUUID();
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.REJECTED);
        updateStatusRequest.setRejectionReason("   ");

        when(harvestRepository.findById(any())).thenReturn(java.util.Optional.of(
                Harvest.builder().id(harvestId).foremanId(foremanId).status(HarvestStatus.PENDING).build()
        ));

        assertThrows(IllegalArgumentException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));
    }

    @Test
    void updateHarvestStatus_ApprovedWithReasonNull() {
        UUID harvestId = UUID.randomUUID();
        updateStatusRequest.setId(harvestId);
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);
        updateStatusRequest.setRejectionReason("Test");

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(foremanId)
                .status(HarvestStatus.PENDING)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(java.util.Optional.of(mockHarvest));

        assertThrows(IllegalArgumentException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateStatus_ApprovedWithBlankReason() {
        updateStatusRequest.setStatus(HarvestStatus.APPROVED);
        updateStatusRequest.setRejectionReason("   ");

        when(harvestRepository.findById(any())).thenReturn(java.util.Optional.of(
                Harvest.builder().foremanId(foremanId).status(HarvestStatus.PENDING).build()
        ));
        when(harvestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> harvestService.updateHarvestStatus(updateStatusRequest, foremanId));
    }

    // GENERAL VIEW ------------------------------------------------------------------
    @Test
    void getHarvestDetail_SuccessAsForeman() {
        UUID harvestId = UUID.randomUUID();
        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .harvesterId(UUID.randomUUID())
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
    void getHarvestDetail_NoIdentity_ThrowsException() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.getHarvestDetail(UUID.randomUUID(), null, null));
    }

    @Test
    void getHarvestDetail_NotFound_ThrowsException() {
        UUID randomId = UUID.randomUUID();
        when(harvestRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(HarvestLogNotFoundException.class, () ->
                harvestService.getHarvestDetail(randomId, null, foremanId));
    }
}