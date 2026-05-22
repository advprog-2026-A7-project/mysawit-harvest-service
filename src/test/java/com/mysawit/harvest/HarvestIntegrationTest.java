package com.mysawit.harvest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.harvest.repository.HarvestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HarvestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HarvestRepository harvestRepository;

    @BeforeEach
    void cleanDatabase() {
        harvestRepository.deleteAll();
    }

    @Test
    void harvestCrudFlowWorksEndToEnd() throws Exception {
        String created = mockMvc.perform(post("/api/harvests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantationId": 11,
                                  "harvestDate": "2026-05-22T07:00:00",
                                  "weight": 500.5,
                                  "quality": "PREMIUM",
                                  "harvesterId": 21,
                                  "notes": "Morning harvest"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plantationId").value(11))
                .andExpect(jsonPath("$.harvestDate").value("2026-05-22T07:00:00"))
                .andExpect(jsonPath("$.weight").value(500.5))
                .andExpect(jsonPath("$.quality").value("PREMIUM"))
                .andExpect(jsonPath("$.harvesterId").value(21))
                .andExpect(jsonPath("$.notes").value("Morning harvest"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/harvests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantationId": 12,
                                  "harvestDate": "2026-05-23T07:00:00",
                                  "weight": 200.0,
                                  "harvesterId": 22
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quality").value("STANDARD"));

        mockMvc.perform(get("/api/harvests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/harvests").param("plantationId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/harvests").param("harvesterId", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/harvests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/api/harvests/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantationId": 15,
                                  "harvestDate": "2026-06-01T10:30:00",
                                  "weight": 750.25,
                                  "quality": "LOW",
                                  "harvesterId": 31,
                                  "notes": "Updated harvest"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plantationId").value(15))
                .andExpect(jsonPath("$.harvestDate").value("2026-06-01T10:30:00"))
                .andExpect(jsonPath("$.weight").value(750.25))
                .andExpect(jsonPath("$.quality").value("LOW"))
                .andExpect(jsonPath("$.harvesterId").value(31))
                .andExpect(jsonPath("$.notes").value("Updated harvest"));

        mockMvc.perform(delete("/api/harvests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Harvest deleted successfully"));

        mockMvc.perform(get("/api/harvests/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Harvest not found with id: " + id));
    }

    @Test
    void validationAndMissingResourcePathsReturnErrors() throws Exception {
        mockMvc.perform(post("/api/harvests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantationId": null,
                                  "harvestDate": null,
                                  "weight": -1
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/harvests/{id}", 404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantationId": 1,
                                  "harvestDate": "2026-05-22T07:00:00",
                                  "weight": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Harvest not found with id: 404"));

        mockMvc.perform(delete("/api/harvests/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Harvest not found with id: 404"));
    }

    @Test
    void healthEndpointReportsServiceName() throws Exception {
        mockMvc.perform(get("/api/harvests/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("mysawit-harvest-service"));
    }
}
