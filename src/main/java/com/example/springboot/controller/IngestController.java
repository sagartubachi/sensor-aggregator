package com.example.springboot.controller;

import com.example.springboot.entity.SensorReading;
import com.example.springboot.service.IngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final IngestService service;

    private static final Logger log = LoggerFactory.getLogger(IngestController.class);

    /*
    This controller accepts IOT readings in an HTTP request. This may not be ideal for production setups.
    I have used this for the prototype. In production we would have a message queue in between the producer (IOT device)
    and the consumer (possibly the /api/ingest API). This API can consume messages in bulk and perform bulk insert in teh DB.
     */
    public IngestController(IngestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestReading(@RequestBody @Validated(SensorReading.Ingest.class) SensorReading req) {
        SensorReading reading = new SensorReading();
        reading.setDeviceId(req.getDeviceId());
        reading.setDeviceType(req.getDeviceType());
        reading.setGroupId(req.getGroupId());
        reading.setMetric(req.getMetric());
        reading.setReading(req.getReading());
        reading.setTs(req.getTs());

        // All the basic validations are happening via the @Validated checks by Spring.
        // More validations can be applied and thrown manually before calling save

        //JpaRepository repo = repositoryFactory.getRepository(reading.getDeviceType());

        service.saveReading(reading);
    }
}
