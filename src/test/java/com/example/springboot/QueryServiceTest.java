package com.example.springboot;

import com.example.springboot.bean.AggregateQueryParams;
import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.entity.SensorReading;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.QueryService;
import com.example.springboot.repository.SensorReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Import(TestSecurityConfig.class)
class QueryServiceTest {

    @Autowired
    private QueryService service;

    @Autowired
    private SensorReadingRepository repository;

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        // Thermostat readings
        repository.save(createReading("thermostat-1", "thermostat", "group-1", "temperature", 22.0));
        repository.save(createReading("thermostat-2", "thermostat", "group-1", "temperature", 24.0));
        repository.save(createReading("thermostat-2", "thermostat", "group-2", "temperature", 25.0));

        // Vehicle readings
        repository.save(createReading("vehicle-1", "vehicle", "group-1", "fuel", 10.0));
        repository.save(createReading("vehicle-2", "vehicle", "group-2", "fuel", 12.0));
    }

    private SensorReading createReading(String deviceId, String deviceType, String groupId, String metric, double value) {
        SensorReading r = new SensorReading();
        r.setDeviceId(deviceId);
        r.setDeviceType(deviceType);
        r.setGroupId(groupId);
        r.setMetric(metric);
        r.setReading(value);
        r.setTs(now);
        return r;
    }

    @Test
    void testNoMatchingRecords() {
        AggregateQueryParams params = new AggregateQueryParams(
                "nonexistent-device", null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                true, true, true, true
        );

        CustomException ex = assertThrows(CustomException.class, () -> service.getAggregates(params));
        assertEquals(CustomException.ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testGroupByDeviceType() {
        AggregateQueryParams params = new AggregateQueryParams(
                null, null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                false, true, false, false
        );

        List<AggregateResponse> results = service.getAggregates(params);
        assertEquals(2, results.size());

        for (AggregateResponse resp : results) {
            switch (resp.getDeviceType()) {
                case "thermostat" -> {
                    assertEquals((22.0 + 24.0 + 25.0) / 3, resp.getAvgValue());
                    assertEquals(22.0, resp.getMinValue());
                    assertEquals(25.0, resp.getMaxValue());
                    assertEquals(3, resp.getCount());
                }
                case "vehicle" -> {
                    assertEquals((10.0 + 12.0) / 2, resp.getAvgValue());
                    assertEquals(10.0, resp.getMinValue());
                    assertEquals(12.0, resp.getMaxValue());
                    assertEquals(2, resp.getCount());
                }
            }
        }
    }

    @Test
    void testGroupByGroupId() {
        AggregateQueryParams params = new AggregateQueryParams(
                null, null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                false, false, true, false
        );

        List<AggregateResponse> results = service.getAggregates(params);
        assertEquals(2, results.size());

        for (AggregateResponse resp : results) {
            switch (resp.getGroupId()) {
                case "group-1" -> {
                    assertEquals((22.0 + 24.0 + 10.0) / 3, resp.getAvgValue());
                    assertEquals(10.0, resp.getMinValue());
                    assertEquals(24.0, resp.getMaxValue());
                    assertEquals(3, resp.getCount());
                }
                case "group-2" -> {
                    assertEquals((25.0 + 12.0) / 2, resp.getAvgValue());
                    assertEquals(12.0, resp.getMinValue());
                    assertEquals(25.0, resp.getMaxValue());
                    assertEquals(2, resp.getCount());
                }
            }
        }
    }

    @Test
    void testGroupByDeviceIdOnly() {
        AggregateQueryParams params = new AggregateQueryParams(
                null, null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                true, false, false, false
        );

        List<AggregateResponse> results = service.getAggregates(params);

        // Should return one row per device
        assertEquals(4, results.size());
    }

    @Test
    void testGroupByMetricOnly() {
        AggregateQueryParams params = new AggregateQueryParams(
                null, null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                false, false, false, true
        );

        List<AggregateResponse> results = service.getAggregates(params);

        // Two metrics: temperature, fuel
        assertEquals(2, results.size());
    }

    @Test
    void testGroupByAllColumns() {
        AggregateQueryParams params = new AggregateQueryParams(
                null, null, null, null,
                now.minusSeconds(60), now,
                "avg,min,max",
                true, true, true, true
        );

        List<AggregateResponse> results = service.getAggregates(params);

        // Each combination of deviceId, deviceType, groupId, metric
        assertEquals(5, results.size());
    }
}