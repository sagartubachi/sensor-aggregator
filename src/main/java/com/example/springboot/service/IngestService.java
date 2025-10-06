package com.example.springboot.service;

import com.example.springboot.entity.SensorReading;
import com.example.springboot.repository.SensorReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
This service is responsible for receiving sensor readings from IOT devices. The timestamp
 */
@Service
public class IngestService {

    private final SensorReadingRepository repository;

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    public IngestService(SensorReadingRepository repository) {
        this.repository = repository;
    }

    // Save a new sensor reading
    // In production systems, Ideally we should save data to shards for scalability. The shard key can either be device
    // type or group depending on the requirement.
    public SensorReading saveReading(SensorReading reading) {

        return repository.save(reading);

        // Not used currently but can be used depending on the shard and aggregate querying requirements
        // I have implemented the SensorReadingRepositoryFactory assuming the shard key as device type.
        // But the sharding mechanism is not integrated end to end.
        // SensorReading repo = repositoryFactory.getRepository(reading.getDeviceType());

        // Wrap blocking JPA call in Mono using boundedElastic scheduler
        /* return Mono.fromCallable(() -> repository.save(reading))
                    .subscribeOn(Schedulers.boundedElastic());*/
        }
}
