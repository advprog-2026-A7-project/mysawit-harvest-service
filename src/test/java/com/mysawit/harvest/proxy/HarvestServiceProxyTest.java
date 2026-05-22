package com.mysawit.harvest.proxy;

import com.mysawit.harvest.dto.*;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import com.mysawit.harvest.service.HarvestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceProxyTest {
    @Mock
    private HarvestServiceImpl harvestServiceImpl;

    @InjectMocks
    private HarvestServiceProxy harvestServiceProxy;

    private UUID validId;
    private LogHarvestRequest logRequest;
    private UpdateHarvestStatusRequest updateRequest;

    @BeforeEach
    void setUp() {
        validId = UUID.randomUUID();
        logRequest = new LogHarvestRequest();
        updateRequest = new UpdateHarvestStatusRequest();
    }

    @Test
    void logHarvest_Success_WhenIdIsNotNull() {
        when(harvestServiceImpl.logHarvest(any(), eq(validId))).thenReturn(new HarvestResponse());

        assertDoesNotThrow(() -> harvestServiceProxy.logHarvest(logRequest, validId));
        verify(harvestServiceImpl, times(1)).logHarvest(logRequest, validId);
    }

    @Test
    void logHarvest_ThrowsException_WhenIdIsNull() {
        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                harvestServiceProxy.logHarvest(logRequest, null)
        );
        assertEquals("Access Denied: Only harvesters are permitted to log harvest data.", ex.getMessage());
        verifyNoInteractions(harvestServiceImpl);
    }

    @Test
    void harvesterView_ThrowsException_WhenIdIsNull() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestServiceProxy.harvesterViewHarvest(new HarvesterViewHarvestRequest(), null)
        );
    }

    @Test
    void harvesterView_Success_WhenIdIsNotNull() {
        HarvesterViewHarvestRequest request = new HarvesterViewHarvestRequest();
        when(harvestServiceImpl.harvesterViewHarvest(any(), eq(validId))).thenReturn(List.of());

        List<HarvestResponse> result = harvestServiceProxy.harvesterViewHarvest(request, validId);

        assertNotNull(result);
        verify(harvestServiceImpl, times(1)).harvesterViewHarvest(request, validId);
    }

    @Test
    void foremanView_Success_WhenIdIsNotNull() {
        when(harvestServiceImpl.foremanViewHarvest(any(), eq(validId))).thenReturn(List.of());

        assertDoesNotThrow(() -> harvestServiceProxy.foremanViewHarvest(new ForemanViewHarvestRequest(), validId));
        verify(harvestServiceImpl).foremanViewHarvest(any(), eq(validId));
    }

    @Test
    void foremanView_ThrowsException_WhenIdIsNull() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestServiceProxy.foremanViewHarvest(new ForemanViewHarvestRequest(), null)
        );
    }

    @Test
    void getDetail_Success_WhenAtLeastOneIdIsPresent() {
        when(harvestServiceImpl.getHarvestDetail(any(), any(), any())).thenReturn(new HarvestResponse());
        assertDoesNotThrow(() -> harvestServiceProxy.getHarvestDetail(UUID.randomUUID(), validId, null));
        assertDoesNotThrow(() -> harvestServiceProxy.getHarvestDetail(UUID.randomUUID(), null, validId));
    }

    @Test
    void getDetail_ThrowsException_WhenBothIdsAreNull() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestServiceProxy.getHarvestDetail(UUID.randomUUID(), null, null)
        );
    }

    @Test
    void updateStatus_Success_WhenForemanIdIsNotNull() {
        when(harvestServiceImpl.updateHarvestStatus(any(), eq(validId))).thenReturn(new HarvestResponse());

        assertDoesNotThrow(() -> harvestServiceProxy.updateHarvestStatus(updateRequest, validId));
        verify(harvestServiceImpl).updateHarvestStatus(updateRequest, validId);
    }

    @Test
    void updateStatus_ThrowsException_WhenForemanIdIsNull() {
        assertThrows(UnauthorizedUserException.class, () ->
                harvestServiceProxy.updateHarvestStatus(updateRequest, null)
        );
    }
}