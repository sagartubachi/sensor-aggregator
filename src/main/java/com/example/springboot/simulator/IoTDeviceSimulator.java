package com.example.springboot.simulator;

import com.example.springboot.entity.SensorReading;
import com.example.springboot.shard.SensorReadingRepositoryFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
This class simulates 3 types of IOT devices - Thermostats, Wearable devices and vehicle sensors.
Each type can have multiple device counts that can be configured in the final properties in this file.
I have just kept these final configurations in the same file for readability purposes. Ideally they would be in the property file.
I have used a ScheduledThreadPool for simulation. This will be a bottleneck in production setup due to the number of threads required.
In a production setup, we would use a message queue like MQTT that is usually used for IOT device use cases.
I have intentionally used threads to keep the setup simple.
 */
@Component
public class IoTDeviceSimulator {

    // All configurations in this simulator can be exported to a properties file for production ready code

    // Run IOT simulation for 30 seconds and stop
    private final long SIMULATION_DURATION_IN_SECONDS = 30;

    // Max number of parallel threads for scheduling reading simulations
    private final int SCHEDULER_POOL_SIZE = 15;

    // Counts of different device types
    private final int THERMOSTAT_SENSOR_COUNT = 5;
    private final int WEARABLE_SENSOR_COUNT = 3;
    private final int VEHICLE_SENSOR_COUNT = 7;

    // Ingestion URL to be called
    private static final String INGESTION_URL = "http://localhost:8080/api/ingest";

    // Configuration: number of devices per type
    private final Map<String, Integer> deviceConfig = Map.of(
            "thermostat", THERMOSTAT_SENSOR_COUNT,
            "wearable", WEARABLE_SENSOR_COUNT,
            "vehicle", VEHICLE_SENSOR_COUNT
    );

    // To generate random readings
    private final Random random = new Random();

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Scheduler to simulate IOT devices for each device type
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(SCHEDULER_POOL_SIZE);

    private static final Logger log = LoggerFactory.getLogger(IoTDeviceSimulator.class);

    // Starts the IOT simulation for each device type
    @PostConstruct
    public void startSimulation() {

        // For every device type, start the IOT device simulation
        deviceConfig.forEach((type, count) -> {

            // The device count will be as per the map configuration
            for (int i = 1; i <= count; i++) {

                // Construct the device id using i
                String deviceId = type + "-" + i;

                // Each IOT device will now emit 1 reading every second, no back pressure, fire and forget semantics
                scheduler.scheduleAtFixedRate(
                        () -> generateReading(deviceId, type),
                        random.nextInt(1000), // Start anytime in the next 1 second
                        1000, // Send a reading every second
                        TimeUnit.MILLISECONDS
                );
            }
        });

        // Schedule shutdown after the configured timeout
        scheduler.schedule(() -> {
            scheduler.shutdown();
            System.out.println("Simulation stopped after " + SIMULATION_DURATION_IN_SECONDS + " seconds.");
        }, SIMULATION_DURATION_IN_SECONDS, TimeUnit.SECONDS);
    }

    private void generateReading(String deviceId, String deviceType) {

        SensorReading reading = new SensorReading();
        reading.setDeviceId(deviceId);
        reading.setDeviceType(deviceType);

        // Generate the group name randomly
        reading.setGroupId("group-" + random.nextInt(5)); // example group assignment

        // Set the metric based on device type
        // This is mainly to make the design extendable to new metrics from the same device type and device id, otherwise this is not necessary
        String metric = switch (deviceType.toLowerCase()) {
            case "thermostat" -> "temperature";
            case "wearable" -> "heart_rate";
            case "vehicle" -> "fuel_consumption";
            default -> "generic_metric";
        };
        reading.setMetric("temperature");

        // Generate meaningful IOT device reading based on device type
        double readingValue = switch (deviceType.toLowerCase()) {
            case "thermostat" -> 20 + random.nextDouble() * 10;     // 20–30 °C
            case "wearable"   -> 60 + random.nextInt(40);    // 60–100 bpm
            case "vehicle"    -> 5 + random.nextDouble() * 15;      // 5–20 L/h
            default           -> random.nextDouble() * 100;         // fallback
        };
        reading.setReading(readingValue);

        reading.setTs(Instant.now());

        // Call the /api/ingest endpoint
        restTemplate().postForObject(INGESTION_URL, reading, Void.class);
    }

}
