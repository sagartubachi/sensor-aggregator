package com.example.springboot.repository;

import com.example.springboot.bean.AggregateQueryParams;
import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.controller.QueryController;
import com.example.springboot.entity.SensorReading;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
I wanted to add a custom dynamically built SQL. So I implemented this interface containing the
added interface method
 */
public class SensorReadingRepositoryImpl implements SensorReadingRepositoryCustom {

    @Autowired
    private EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(SensorReadingRepositoryImpl.class);

    /*
    Generate a dynamic SQL to handle custom aggregates using conditional grouping based on criteria
     */
    public List<AggregateResponse> getDynamicAggregates(AggregateQueryParams params) {

        // Get selection and grouping params
        String deviceId = params.deviceId();
        String deviceType = params.deviceType();
        String groupId = params.groupId();
        String metric = params.metric();
        Instant start = params.from();
        Instant end = params.to();

        boolean groupByDeviceId = params.groupByDeviceId();
        boolean groupByDeviceType = params.groupByDeviceType();
        boolean groupByGroupId = params.groupByGroupId();
        boolean groupByMetric = params.groupByMetric();

        // SELECT columns
        List<String> selectCols = new ArrayList<>();
        selectCols.add(groupByDeviceId ? "sr.deviceId" : "'ALL' as deviceId");
        selectCols.add(groupByDeviceType ? "sr.deviceType" : "'ALL' as deviceType");
        selectCols.add(groupByGroupId ? "sr.groupId" : "'ALL' as groupId");
        selectCols.add(groupByMetric ? "sr.metric" : "'ALL' as metric");
        selectCols.add("AVG(sr.reading) as avgValue");
        selectCols.add("MIN(sr.reading) as minValue");
        selectCols.add("MAX(sr.reading) as maxValue");
        selectCols.add("MEDIAN(sr.reading) as medianValue");
        selectCols.add("COUNT(*) as countValue");

        // Group By columns
        List<String> groupCols = new ArrayList<>();
        if(groupByDeviceId) groupCols.add("sr.deviceId");
        if(groupByDeviceType) groupCols.add("sr.deviceType");
        if(groupByGroupId) groupCols.add("sr.groupId");
        if(groupByMetric) groupCols.add("sr.metric");

        // Start building SQL
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", selectCols));
        sql.append(" FROM SensorReading sr WHERE sr.ts BETWEEN :start AND :end ");

        // Add filters
        if (deviceId != null && !deviceId.isBlank()) sql.append("AND sr.deviceId = :deviceId ");
        if (deviceType != null && !deviceType.isBlank()) sql.append("AND sr.deviceType = :deviceType ");
        if (groupId != null && !groupId.isBlank()) sql.append("AND sr.groupId = :groupId ");
        if (metric != null && !metric.isBlank()) sql.append("AND sr.metric = :metric ");

        // Add conditional grouping
        if (!groupCols.isEmpty()) {
            sql.append("GROUP BY ").append(String.join(", ", groupCols));
        }

        // Create Query
        jakarta.persistence.Query query = em.createQuery(sql.toString());

        // Substitute placeholders
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (deviceId != null && !deviceId.isBlank()) query.setParameter("deviceId", deviceId);
        if (deviceType != null && !deviceType.isBlank()) query.setParameter("deviceType", deviceType);
        if (groupId != null && !groupId.isBlank()) query.setParameter("groupId", groupId);
        if (metric != null && !metric.isBlank()) query.setParameter("metric", metric);

        // Execute query
        List<Object[]> results = query.getResultList();

        // In case on empty records, JPA returns a single row if aggregate functions are used with null as values for aggregates like average, sum etc
        // Skipping such rows using filter
        return results.stream()
                .filter(arr -> arr[4] != null) // skip rows where avg/min/max are null
                .map(arr -> new AggregateResponse(
                        (String) arr[0],
                        (String) arr[1],
                        (String) arr[2],
                        (String) arr[3],
                        ((Number) arr[4]).doubleValue(),
                        ((Number) arr[5]).doubleValue(),
                        ((Number) arr[6]).doubleValue(),
                        ((Number) arr[7]).doubleValue(),
                        ((Number) arr[8]).longValue(),
                        start,
                        end
                ))
                .collect(Collectors.toList());
    }
}