package com.example.backend.controller;

import com.example.backend.entity.HabitAnalytics;
import com.example.backend.entity.HabitCorrelation;
import com.example.backend.repository.HabitAnalyticsRepository;
import com.example.backend.repository.HabitCorrelationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AnalyticsController.
 */
@SpringBootTest
@SpringJUnitConfig
@AutoConfigureWebMvc
@Transactional
class AnalyticsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;

    @Autowired
    private HabitCorrelationRepository habitCorrelationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private UUID userId;
    private UUID habitId1;
    private UUID habitId2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        userId = UUID.randomUUID();
        habitId1 = UUID.randomUUID();
        habitId2 = UUID.randomUUID();
        
        // Create test data
        createTestAnalyticsData();
        createTestCorrelationData();
    }

    @Test
    void testGetUserHabitAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].habitId").exists())
                .andExpect(jsonPath("$[0].successRate").exists())
                .andExpect(jsonPath("$[0].consistencyScore").exists())
                .andExpect(jsonPath("$[0].formationStage").exists());
    }

    @Test
    void testGetHabitAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}", userId, habitId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.habitId").value(habitId1.toString()))
                .andExpect(jsonPath("$.successRate").value(0.85))
                .andExpect(jsonPath("$.consistencyScore").value(0.75))
                .andExpect(jsonPath("$.formationStage").value("LEARNING"));
    }

    @Test
    void testGetHabitAnalyticsNotFound() throws Exception {
        UUID nonExistentHabitId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}", userId, nonExistentHabitId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetHabitsByFormationStage() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/formation-stage/{stage}", userId, "LEARNING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].formationStage").value("LEARNING"));
    }

    @Test
    void testGetHabitsByFormationStageInvalid() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/formation-stage/{stage}", userId, "INVALID_STAGE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetHighPerformingHabits() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/high-performing", userId)
                .param("threshold", "0.7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Only habit1 has success rate >= 0.7
                .andExpect(jsonPath("$[0].successRate").value(0.85));
    }

    @Test
    void testGetConsistentHabits() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/consistent", userId)
                .param("threshold", "0.7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Only habit1 has consistency score >= 0.7
                .andExpect(jsonPath("$[0].consistencyScore").value(0.75));
    }

    @Test
    void testGetHabitCorrelations() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/correlations", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].correlationCoefficient").value(0.65))
                .andExpect(jsonPath("$[0].correlationType").value("POSITIVE"));
    }

    @Test
    void testGetPositiveCorrelations() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/correlations/positive", userId)
                .param("threshold", "0.5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].correlationCoefficient").value(0.65));
    }

    @Test
    void testGetNegativeCorrelations() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/correlations/negative", userId)
                .param("threshold", "-0.5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); // No negative correlations in test data
    }

    @Test
    void testGetHabitCorrelationsForSpecificHabit() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}/correlations", userId, habitId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAnalyticsSummary() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/summary", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.totalHabits").value(2))
                .andExpect(jsonPath("$.totalCorrelations").value(1))
                .andExpect(jsonPath("$.averageSuccessRate").exists())
                .andExpect(jsonPath("$.averageConsistencyScore").exists())
                .andExpect(jsonPath("$.habitsByFormationStage").exists());
    }

    @Test
    void testGetOptimalTiming() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}/optimal-timing", userId, habitId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.habitId").value(habitId1.toString()))
                .andExpect(jsonPath("$.hourlyStats").exists())
                .andExpect(jsonPath("$.dayOfWeekStats").exists());
    }

    @Test
    void testGetOptimalTimingWithDateRange() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}/optimal-timing", userId, habitId1)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.habitId").value(habitId1.toString()));
    }

    @Test
    void testGetBestTimeWindows() throws Exception {
        mockMvc.perform(get("/api/analytics/users/{userId}/habits/{habitId}/best-times", userId, habitId1)
                .param("count", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAnalyzeCorrelations() throws Exception {
        mockMvc.perform(post("/api/analytics/users/{userId}/correlations/analyze", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Correlation analysis completed")));
    }

    @Test
    void testAnalyzeCorrelationsWithDateRange() throws Exception {
        mockMvc.perform(post("/api/analytics/users/{userId}/correlations/analyze", userId)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Correlation analysis completed")));
    }

    // Helper methods

    private void createTestAnalyticsData() {
        // Create analytics for habit1
        HabitAnalytics analytics1 = new HabitAnalytics(userId, habitId1);
        analytics1.setSuccessRate(0.85);
        analytics1.setConsistencyScore(0.75);
        analytics1.setOptimalTimeStart(LocalTime.of(8, 0));
        analytics1.setOptimalTimeEnd(LocalTime.of(9, 0));
        analytics1.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        analytics1.setHabitStrength(0.70);
        habitAnalyticsRepository.save(analytics1);

        // Create analytics for habit2
        HabitAnalytics analytics2 = new HabitAnalytics(userId, habitId2);
        analytics2.setSuccessRate(0.60);
        analytics2.setConsistencyScore(0.65);
        analytics2.setOptimalTimeStart(LocalTime.of(20, 0));
        analytics2.setOptimalTimeEnd(LocalTime.of(21, 0));
        analytics2.setFormationStage(HabitAnalytics.FormationStage.STABILITY);
        analytics2.setHabitStrength(0.55);
        habitAnalyticsRepository.save(analytics2);
    }

    private void createTestCorrelationData() {
        // Create correlation between habit1 and habit2
        HabitCorrelation correlation = new HabitCorrelation(userId, habitId1, habitId2, 0.65, HabitCorrelation.CorrelationType.POSITIVE);
        correlation.setConfidenceLevel(0.85);
        habitCorrelationRepository.save(correlation);
    }
}