package com.example.springboot;

import com.example.springboot.entity.SensorReading;
import com.example.springboot.repository.SensorReadingRepository;
import com.example.springboot.service.IngestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IngestServiceTest {

    @Autowired
    private SensorReadingRepository repository;

    private IngestService service;

    private Instant now;

    @BeforeEach
    void setUp() {
        service = new IngestService(repository);
        now = Instant.now();
        repository.deleteAll();
    }

    @Test
    void testSaveReading_Success() {
        SensorReading reading = new SensorReading();
        reading.setDeviceId("thermostat-1");
        reading.setDeviceType("thermostat");
        reading.setGroupId("group-1");
        reading.setMetric("temperature");
        reading.setReading(25.5);
        reading.setTs(now);

        SensorReading saved = service.saveReading(reading);

        assertNotNull(saved.getId(), "Saved reading should have generated ID");
        assertEquals("thermostat-1", saved.getDeviceId());
        assertEquals(25.5, saved.getReading());

        // Verify it's actually persisted in DB
        Optional<SensorReading> fromDb = repository.findById(saved.getId());
        assertTrue(fromDb.isPresent());
        assertEquals("thermostat-1", fromDb.get().getDeviceId());
    }

    @Test
    void testSaveMultipleReadings() {

        SensorReading r1 = new SensorReading();
        r1.setDeviceId("thermostat-1");
        r1.setDeviceType("thermostat");
        r1.setGroupId("group-1");
        r1.setMetric("temperature");
        r1.setReading(25.5);
        r1.setTs(now);

        SensorReading r2 = new SensorReading();
        r2.setDeviceId("vehicle-1");
        r2.setDeviceType("vehicle");
        r2.setGroupId("group-1");
        r2.setMetric("fuel");
        r2.setReading(10.0);
        r2.setTs(now);

        service.saveReading(r1);
        service.saveReading(r2);

        assertEquals(2, repository.count());
    }

    @Test
    void testSaveReading_MissingRequiredField() {
        SensorReading reading = new SensorReading();
        reading.setDeviceId(null); // deviceId is mandatory
        reading.setDeviceType("thermostat");
        reading.setGroupId("group-1");
        reading.setMetric("temperature");
        reading.setReading(25.0);
        reading.setTs(now);

        // JPA will throw exception for null deviceId
        assertThrows(Exception.class, () -> service.saveReading(reading));
    }
}