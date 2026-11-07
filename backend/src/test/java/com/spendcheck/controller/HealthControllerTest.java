package com.spendcheck.controller;

import com.spendcheck.config.CorsConfig;
import com.spendcheck.config.SecurityConfig;
import com.spendcheck.dto.HealthResponse;
import com.spendcheck.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @Test
    void healthEndpointShouldBePubliclyAccessible() throws Exception {
        when(healthService.checkHealth()).thenReturn(new HealthResponse(
                "UP",
                "SpendCheck Backend API",
                "PostgreSQL 17.0 (Connected)",
                "17.0"
        ));

        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("SpendCheck Backend API"))
                .andExpect(jsonPath("$.data.database").value("PostgreSQL 17.0 (Connected)"));
    }
}
