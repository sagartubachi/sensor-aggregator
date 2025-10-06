package com.example.springboot.shard;

import com.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

// This class is not used currently as I do not want to assume the requirements on the shard key.
// If the shard key would be device type, here is a sample implementation of factory pattern
// which can be used by the IngestService and QueryService to get the right shard on which data can be saved / queried
@Component
public class SensorReadingRepositoryFactory {

    private final Map<String, SensorRepository> repositoryMap;

    private static final Logger log = LoggerFactory.getLogger(SensorReadingRepositoryFactory.class);

    // The map will ideally contain 3 implementations of the JPA Repository. Each will save data
    // in their respective shard / table in prototype
    @Autowired
    public SensorReadingRepositoryFactory(Map<String, SensorRepository> repositoryMap) {
        this.repositoryMap = repositoryMap;
    }

    public SensorRepository getRepository(String deviceType) {
        switch (deviceType.toLowerCase()) {
            case "thermostat":
                return repositoryMap.get("thermostatRepo");
            case "wearable":
                return repositoryMap.get("wearableRepo");
            case "camera":
                return repositoryMap.get("cameraRepo");
            default:
                throw new IllegalArgumentException("Unknown device type: " + deviceType);
        }
    }

}
