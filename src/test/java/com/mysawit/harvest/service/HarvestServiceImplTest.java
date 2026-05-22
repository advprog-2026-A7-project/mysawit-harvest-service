package com.mysawit.harvest.service;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.event.HarvestPayrollEventPublisher;
import com.mysawit.harvest.exception.HarvestLogNotFoundException;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.mapper.HarvestMapper;
import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import com.mysawit.harvest.model.UserReplica;
import com.mysawit.harvest.repository.HarvestRepository;

import com.mysawit.harvest.dto.HarvesterContext;
import com.mysawit.harvest.service.validation.HarvestValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private HarvestPayrollEventPublisher harvestPayrollEventPublisher;

    @Mock
    private HarvestValidationChain harvestValidationChain;

    @Mock
    private HarvesterContextService harvesterContextService;

    @Mock
    private com.mysawit.harvest.repository.UserReplicaRepository userReplicaRepository;

    @InjectMocks
    private HarvestServiceImpl harvestService;

    private UUID harvesterId;
    private UUID foremanId;
    private String plantationId;
    private UUID harvestId;
    private String harvesterName;

    private LogHarvestRequest logRequest;
    private HarvesterViewHarvestRequest harvesterViewRequest;
    private UpdateHarvestStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        plantationId = "PLT-12345678";
        harvestId = UUID.randomUUID();
        harvesterName = "Strawberry Shortcake";

        logRequest = new LogHarvestRequest();
        logRequest.setPlantationId(plantationId);
        logRequest.setWeight(777.0);
        logRequest.setNews("Successful harvest");

        harvesterViewRequest = new HarvesterViewHarvestRequest();

        updateStatusRequest = new UpdateHarvestStatusRequest();
    }

    private void mockForemanValidation(UUID fid) {
        UserReplica mockForeman = UserReplica.builder()
                .id(fid)
                .plantationId("PLT-12345678")
                .build();

        when(userReplicaRepository.findById(fid)).thenReturn(Optional.of(mockForeman));
    }

    private void mockValidationChain() {
        doNothing().when(harvestValidationChain).validate(logRequest, harvesterId);
    }

    @Test
    void logHarvest_Success() {
        mockValidationChain();

        when(harvesterContextService.resolve(harvesterId)).thenReturn(new HarvesterContext(harvesterName, foremanId));
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HarvestResponse response = harvestService.logHarvest(logRequest, harvesterId);

        assertNotNull(response);
        assertEquals(harvesterId, response.getHarvesterId());
        assertEquals(harvesterName, response.getHarvesterName());
        assertEquals(foremanId, response.getForemanId());
        assertEquals(HarvestStatus.PENDING, response.getStatus());

        verify(harvesterContextService).resolve(harvesterId);
        verify(harvestRepository).save(any(Harvest.class));
    }

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

    @Test
    void foremanViewHarvest_Success() {
        mockForemanValidation(foremanId);

        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();
        req.setHarvesterName("Strawberry Shortcake");
        req.setDate(LocalDate.now());

        when(harvestRepository.findAllByHarvesterNameAndDate(
                eq(foremanId), any(), any()))
                .thenReturn(List.of(new Harvest(), new Harvest()));

        List<HarvestResponse> responses = harvestService.foremanViewHarvest(req, foremanId);

        assertEquals(2, responses.size());
        verify(harvestRepository).findAllByHarvesterNameAndDate(
                eq(foremanId), eq("Strawberry Shortcake"), any());
    }

    @Test
    void updateHarvestStatus_Success_Approved_WithRabbitMQ() {
        mockForemanValidation(foremanId);

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

        verify(harvestPayrollEventPublisher, times(1)).publishApprovedHarvest(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_WrongForeman() {
        mockForemanValidation(foremanId);

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
        mockForemanValidation(foremanId);

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
        mockForemanValidation(foremanId);

        UUID randomId = UUID.randomUUID();
        updateStatusRequest.setId(randomId);

        when(harvestRepository.findById(eq(randomId))).thenReturn(java.util.Optional.empty());

        assertThrows(HarvestLogNotFoundException.class, () ->
                harvestService.updateHarvestStatus(updateStatusRequest, foremanId));

        verify(harvestRepository, never()).save(any());
    }

    @Test
    void updateHarvestStatus_Rejected_ShouldNotSendRabbitMQMessage() {
        mockForemanValidation(foremanId);

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

        verify(harvestPayrollEventPublisher, never()).publishApprovedHarvest(any(Harvest.class));
    }

    @Test
    void updateHarvestStatus_InvalidTargetStatus_ThrowsException() {
        mockForemanValidation(foremanId);

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
        verify(harvestPayrollEventPublisher, never()).publishApprovedHarvest(any(Harvest.class));
    }

    @Test
    void getHarvestDetail_SuccessAsForeman() {
        mockForemanValidation(foremanId);

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
        mockForemanValidation(foremanId);

        UUID randomId = UUID.randomUUID();
        when(harvestRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(HarvestLogNotFoundException.class, () ->
                harvestService.getHarvestDetail(randomId, null, foremanId));
    }

    @Test
    void getHarvestDetail_ForbiddenForOtherForeman() {
        UUID actualForemanId = UUID.randomUUID();
        UUID intruderForemanId = UUID.randomUUID();

        mockForemanValidation(intruderForemanId);

        Harvest mockHarvest = Harvest.builder()
                .id(harvestId)
                .foremanId(actualForemanId)
                .build();

        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mockHarvest));

        assertThrows(UnauthorizedUserException.class, () ->
                harvestService.getHarvestDetail(harvestId, null, intruderForemanId));
    }

    @Test
    void harvesterViewHarvest_ThrowsIllegalArgumentException_WhenEndDateIsBeforeStartDate() {
        HarvesterViewHarvestRequest invalidDateRequest = new HarvesterViewHarvestRequest();

        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 19, 10, 0);

        invalidDateRequest.setStartDate(start);
        invalidDateRequest.setEndDate(end);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                harvestService.harvesterViewHarvest(invalidDateRequest, harvesterId));

        assertEquals("End Date must not be before Start Date", exception.getMessage());
        verifyNoInteractions(harvestRepository);
    }

    @Test
    void harvesterViewHarvest_PassesValidation_WhenDatesAreValidOrNull() {
        HarvesterViewHarvestRequest validDateRequest = new HarvesterViewHarvestRequest();
        validDateRequest.setStartDate(LocalDateTime.now());
        validDateRequest.setEndDate(null);

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                harvestService.harvesterViewHarvest(validDateRequest, harvesterId));
    }

    @Test
    void harvesterViewHarvest_DateValidation_FalseWhenStartDateIsNull() {
        HarvesterViewHarvestRequest request = new HarvesterViewHarvestRequest();
        request.setStartDate(null);
        request.setEndDate(LocalDateTime.now());

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> harvestService.harvesterViewHarvest(request, harvesterId));
    }

    @Test
    void harvesterViewHarvest_DateValidation_FalseWhenEndDateIsNull() {
        HarvesterViewHarvestRequest request = new HarvesterViewHarvestRequest();
        request.setStartDate(LocalDateTime.now());
        request.setEndDate(null);

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> harvestService.harvesterViewHarvest(request, harvesterId));
    }

    @Test
    void harvesterViewHarvest_DateValidation_FalseWhenBothDatesAreNull() {
        HarvesterViewHarvestRequest request = new HarvesterViewHarvestRequest();
        request.setStartDate(null);
        request.setEndDate(null);

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> harvestService.harvesterViewHarvest(request, harvesterId));
    }

    @Test
    void harvesterViewHarvest_DateValidation_FalseWhenEndDateIsAfterStartDate() {
        HarvesterViewHarvestRequest request = new HarvesterViewHarvestRequest();
        request.setStartDate(LocalDateTime.of(2026, 5, 20, 8, 0));
        request.setEndDate(LocalDateTime.of(2026, 5, 20, 17, 0));

        when(harvestRepository.findAllByHarvesterIdAndDateAndStatus(eq(harvesterId), any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> harvestService.harvesterViewHarvest(request, harvesterId));
    }

    @Test
    void foremanViewHarvest_ThrowsException_WhenForemanNotFound() {
        when(userReplicaRepository.findById(foremanId)).thenReturn(Optional.empty());

        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, foremanId));

        assertEquals("Foreman not found.", exception.getMessage());
        verifyNoInteractions(harvestRepository);
    }

    @Test
    void foremanViewHarvest_ThrowsException_WhenPlantationIdIsNull() {
        UserReplica mockForemanWithoutPlantation = UserReplica.builder()
                .id(foremanId)
                .plantationId(null)
                .build();

        when(userReplicaRepository.findById(foremanId)).thenReturn(Optional.of(mockForemanWithoutPlantation));

        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, foremanId));

        assertEquals("Foreman is not assigned to any plantation.", exception.getMessage());
        verifyNoInteractions(harvestRepository);
    }

    @Test
    void foremanViewHarvest_ThrowsException_WhenPlantationIdIsEmptyOrSpaces() {
        UserReplica mockForemanWithEmptyPlantation = UserReplica.builder()
                .id(foremanId)
                .plantationId("   ")
                .build();

        when(userReplicaRepository.findById(foremanId)).thenReturn(Optional.of(mockForemanWithEmptyPlantation));

        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () ->
                harvestService.foremanViewHarvest(req, foremanId));

        assertEquals("Foreman is not assigned to any plantation.", exception.getMessage());
        verifyNoInteractions(harvestRepository);
    }

    @Test
    void foremanViewHarvest_ThrowsException_WhenForemanIdIsNull() {
        ForemanViewHarvestRequest req = new ForemanViewHarvestRequest();

        assertDoesNotThrow(() -> {
            when(harvestRepository.findAllByHarvesterNameAndDate(any(), any(), any()))
                    .thenReturn(List.of());

            harvestService.foremanViewHarvest(req, null);
        });

        verify(harvestRepository).findAllByHarvesterNameAndDate(eq(null), any(), any());
        verifyNoInteractions(userReplicaRepository);
    }
}