package com.example.springboot;

import com.example.springboot.controller.IngestController;
import com.example.springboot.entity.SensorReading;
import com.example.springboot.security.JwtService;
import com.example.springboot.service.IngestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestController.class)
@Import(TestSecurityConfig.class)
class IngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngestService service;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        when(jwtService.generateToken(anyString())).thenReturn("dummyToken");
    }

    // Register Java 8 date/time module
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void testIngestReading_Success() throws Exception {
        SensorReading reading = new SensorReading();
        reading.setDeviceId("device-1");
        reading.setDeviceType("thermostat");
        reading.setGroupId("group-1");
        reading.setMetric("temperature");
        reading.setReading(25.5);
        reading.setTs(Instant.now());

        mockMvc.perform(post("/api/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reading))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1OTU3MDM1NiwiZXhwIjoxNzU5NjU2NzU2LCJpc3MiOiJpb3Qtc2Vuc29yLWFwcCJ9.n4F6RF6X5Dlxp2fUqHZ9ViBicsUQ2bCXG3o5An667sU"))
                .andExpect(status().isCreated());

        // Verify that the service.saveReading() was called once with any SensorReading
        verify(service, times(1)).saveReading(any(SensorReading.class));
    }

    @Test
    void testIngestReading_MissingRequiredFields() throws Exception {
        SensorReading reading = new SensorReading(); // no fields set

        mockMvc.perform(post("/api/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reading)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testIngestReading_InvalidFieldType() throws Exception {
        // Sending a JSON with wrong field type for "reading" (String instead of Double)
        String invalidJson = """
                {
                    "deviceId": "device-1",
                    "deviceType": "thermostat",
                    "groupId": "group-1",
                    "metric": "temperature",
                    "reading": "invalid-number",
                    "ts": "2025-10-03T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
