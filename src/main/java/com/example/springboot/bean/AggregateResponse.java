package com.example.springboot.bean;

import java.time.Instant;

/*
This class will be used while responding to the aggregate sensor data requests
 */
public class AggregateResponse {
    private String deviceId;
    private String deviceType;
    private String groupId;
    private String metric;
    private Double avgValue;
    private Double minValue;
    private Double maxValue;
    private Double medianValue;
    private Long count;

    public AggregateResponse() {}

    public AggregateResponse(String deviceId, String deviceType, String groupId, String metric,
                             Double avgValue, Double minValue, Double maxValue, Double medianValue,
                             Long count) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.groupId = groupId;
        this.metric = metric;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.medianValue = medianValue;
        this.count = count;
    }

    // --- Getters & Setters ---

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMedianValue() { return medianValue; }

    public void setMedianValue(Double medianValue) { this.medianValue = medianValue; }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
