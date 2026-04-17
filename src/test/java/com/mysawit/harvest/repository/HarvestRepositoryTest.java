package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.Harvest;
import com.mysawit.harvest.model.HarvestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class HarvestRepositoryTest {

    @Autowired
    private HarvestRepository harvestRepository;

    private UUID harvesterId;
    private UUID foremanId;
    private Harvest harvest1;
    private Harvest harvest2;

    private List<Harvest> findHarvesterHarvests(UUID id, LocalDateTime start, LocalDateTime end) {
        return harvestRepository.findAllByHarvesterIdAndDate(
                id,
                start != null ? start : LocalDateTime.of(2000, 1, 1, 0, 0),
                end != null ? end : LocalDateTime.of(2100, 1, 1, 0, 0)
        );
    }

    private List<Harvest> findForemanHarvests(UUID id, String name, LocalDateTime start, LocalDateTime end) {
        return harvestRepository.findAllByHarvesterNameAndDate(
                id,
                (name != null && !name.isBlank()) ? name : "",
                start != null ? start : LocalDateTime.of(2000, 1, 1, 0, 0),
                end != null ? end : LocalDateTime.of(2100, 1, 1, 0, 0)
        );
    }

    @BeforeEach
    void setUp() {
        harvesterId = UUID.randomUUID();
        foremanId = UUID.randomUUID();
        UUID plantationId = UUID.randomUUID();

        harvest1 = Harvest.builder()
                .harvesterId(harvesterId)
                .foremanId(foremanId)
                .harvesterName("Strawberry Shortcake")
                .plantationId(plantationId)
                .news("Harvest 1")
                .harvestDate(LocalDateTime.of(2026, 3, 1, 10, 0))
                .weight(777.0)
                .status(HarvestStatus.APPROVED)
                .build();

        harvest2 = Harvest.builder()
                .harvesterId(harvesterId)
                .foremanId(foremanId)
                .harvesterName("Strawberry Shortcake")
                .plantationId(plantationId)
                .news("Harvest 2")
                .harvestDate(LocalDateTime.of(2026, 3, 15, 10, 0))
                .weight(888.0)
                .status(HarvestStatus.PENDING)
                .build();

        harvestRepository.saveAll(List.of(harvest1, harvest2));
    }

    // HARVEST LOG ------------------------------------------------------------------
    @Test
    void existsHarvestByHarvesterIdAndHarvestDateBetween_ReturnsTrue_WhenExists() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 1, 23, 59);

        boolean exists = harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(harvesterId, start, end);

        assertTrue(exists);
    }

    @Test
    void existsHarvestByHarvesterIdAndHarvestDateBetween_ReturnsFalse_WhenNotExists() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 1, 23, 59);

        boolean exists = harvestRepository.existsHarvestByHarvesterIdAndHarvestDateBetween(harvesterId, start, end);

        assertFalse(exists);
    }

    // HARVESTER VIEW ------------------------------------------------------------------
    @Test
    void findAllByHarvesterIdAndDate_ShowAll_WhenDatesAreNull() {
        List<Harvest> results = findHarvesterHarvests(harvesterId, null, null);
        assertEquals(2, results.size());
    }

    @Test
    void findAllByHarvesterIdAndDate_OnlyStartDate() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 0, 0);
        List<Harvest> results = findHarvesterHarvests(harvesterId, start, null);
        assertEquals(1, results.size());
        assertEquals(harvest2.getHarvestDate(), results.getFirst().getHarvestDate());
    }

    @Test
    void findAllByHarvesterIdAndDate_OnlyEndDate() {
        LocalDateTime end = LocalDateTime.of(2026, 3, 5, 23, 59);
        List<Harvest> results = findHarvesterHarvests(harvesterId, null, end);
        assertEquals(1, results.size());
        assertEquals(harvest1.getHarvestDate(), results.getFirst().getHarvestDate());
    }

    @Test
    void findAllByHarvesterIdAndDate_PassAllDateFilter() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 20, 0, 0);
        List<Harvest> results = findHarvesterHarvests(harvesterId, start, end);
        assertEquals(2, results.size());
    }

    // FOREMAN VIEW ------------------------------------------------------------------
    @Test
    void findAllForemanFiltered_NoFilter_ReturnAll() {
        List<Harvest> results = findForemanHarvests(foremanId, null, null, null);
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByName_Found() {
        List<Harvest> results = findForemanHarvests(foremanId, "strawberry", null, null);
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByName_NotFound() {
        List<Harvest> results = findForemanHarvests(foremanId, "mango", null, null);
        assertEquals(0, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, null,
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 20, 0, 0));
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByDate_NotFound() {
        List<Harvest> results = findForemanHarvests(
                foremanId, null,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0));
        assertEquals(0, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByNameAndDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, "strawberry",
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 20, 0, 0));
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_FilterByNameAndDate_NotFound() {
        List<Harvest> results = findForemanHarvests(
                foremanId, "raspberry",
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 20, 0, 0));
        assertEquals(0, results.size());
    }

    @Test
    void findAllForemanFiltered_OnlyStartDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, null,
                LocalDateTime.of(2026, 3, 1, 0, 0),
                null);
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_OnlyEndDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, null,
                null,
                LocalDateTime.of(2026, 3, 31, 23, 59));
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_NameAndStartDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, "berry",
                LocalDateTime.of(2026, 3, 1, 0, 0),
                null);
        assertEquals(2, results.size());
    }

    @Test
    void findAllForemanFiltered_NameAndEndDate_Found() {
        List<Harvest> results = findForemanHarvests(
                foremanId, "straw",
                null,
                LocalDateTime.of(2026, 3, 31, 23, 59));
        assertEquals(2, results.size());
    }
}