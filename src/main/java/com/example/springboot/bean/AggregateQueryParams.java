package com.example.springboot.bean;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;

/*
The class that is used as criteria while getting data from the system.
Few validations are happening using @NotNull annotation. Spring will throw exceptions which will be caught by the global exception handler
 */
@Validated(Query.class)
public record AggregateQueryParams(
        String deviceId,
        String deviceType,
        String groupId,
        String metric,
        @NotNull Instant from,
        @NotNull Instant to,
        String agg,
        Boolean groupByDeviceId,
        Boolean groupByDeviceType,
        Boolean groupByGroupId,
        Boolean groupByMetric
) {
    // Factory constructor to provide defaults for optional fields
    public AggregateQueryParams {

        if (agg == null || agg.isBlank()) {
            agg = "avg,min,max,median";
        }

        if (groupByDeviceId == null) groupByDeviceId = false;
        if (groupByDeviceType == null) groupByDeviceType = false;
        if (groupByGroupId == null) groupByGroupId = false;
        if (groupByMetric == null) groupByMetric = false;
    }
}
