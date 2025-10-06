package com.example.springboot.entity;

import com.example.springboot.bean.AggregateResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/*
The class that represents the table in H2 in which the readings would be stored
Mandatory parameter validations are happening in the bean itself via @NotNull
These exceptions are caught in the global exception handler.
The timestamp is intentionally kept as mandatory as there can be network delays from producer (IOT device)
to the consumer (Ingestion service)
 */
@Entity
@Table(
        name = "sensor_reading",
        indexes = {
                @Index(name = "idx_device_id_ts", columnList = "deviceId, ts"),
                @Index(name = "idx_device_type_ts", columnList = "deviceType, ts"),
                @Index(name = "idx_group_ts", columnList = "groupId, ts")
        }
)
public class SensorReading {

    // Marker interfaces for specific validations for ingestController and not the QueryController
    public interface Ingest {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(groups = Ingest.class)
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @NotNull(groups = Ingest.class)
    @Column(name = "device_type", nullable = false, length = 100)
    private String deviceType;

    @NotNull(groups = Ingest.class)
    @Column(name = "group_id", nullable = false, length = 100)
    private String groupId;

    @NotNull(groups = Ingest.class)
    @Column(nullable = false, length = 100)
    private String metric;

    @NotNull(groups = Ingest.class)
    @Column(nullable = false)
    private Double reading;

    @NotNull(groups = Ingest.class)
    @Column(nullable = false)
    private Instant ts;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public Double getReading() { return reading; }
    public void setReading(Double reading) { this.reading = reading; }

    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }
}
