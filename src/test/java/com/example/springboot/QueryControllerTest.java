package com.example.springboot;

import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.controller.QueryController;
import com.example.springboot.security.JwtService;
import com.example.springboot.service.QueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
@Import(TestSecurityConfig.class)
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService service;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        when(jwtService.generateToken(anyString())).thenReturn("dummyToken");
    }

    @Test
    void testQueryAggregates_WithResults() throws Exception {

        List<AggregateResponse> mockResult = List.of(
                new AggregateResponse(
                        "device-1", "thermostat", "group-1", "temperature",
                        25.0, 20.0, 30.0, 25.0, 5L,
                        Instant.parse("2025-10-03T00:00:00Z"),
                        Instant.parse("2025-10-03T23:59:59Z")
                )
        );

        when(service.getAggregates(any())).thenReturn(mockResult);

        mockMvc.perform(get("/api/query")
                        .param("from", "2025-10-03T00:00:00Z")
                        .param("to", "2025-10-03T23:59:59Z")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("device-1"))
                .andExpect(jsonPath("$[0].avgValue").value(25.0));
    }

    @Test
    void testQueryAggregates_NoResults() throws Exception {
        when(service.getAggregates(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/query")
                        .param("from", "2025-10-03T00:00:00Z")
                        .param("to", "2025-10-03T23:59:59Z")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"No matching records found\"}"));
    }

    @Test
    void testQueryAggregates_MissingRequiredParam() throws Exception {
        mockMvc.perform(get("/api/query")
                        .param("to", "2025-10-03T23:59:59Z")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.from").exists());
    }

    @Test
    void testQueryAggregates_InvalidParamType() throws Exception {
        mockMvc.perform(get("/api/query")
                        .param("from", "invalid-date")
                        .param("to", "2025-10-03T23:59:59Z")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                // check that the JSON contains an "errors" object with a key "from"
                .andExpect(jsonPath("$.errors.from").exists());
    }

    @Test
    void testQueryAggregates_GroupingFlags() throws Exception {
        when(service.getAggregates(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/query")
                        .param("from", "2025-10-03T00:00:00Z")
                        .param("to", "2025-10-03T23:59:59Z")
                        .param("groupByDeviceId", "true")
                        .param("groupByMetric", "true")
                )
                .andExpect(status().isOk());
    }
}
