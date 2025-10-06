package com.example.springboot.repository;

import com.example.springboot.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/*
This is the class that interacts with the H2 table.
Since H2 does not support dynamic group by clauses using "When" clause, the getAggregatesGrouped is not being used in /api/query currently.
The API instead uses a dynamically generated SQL to run native queries.
Most of these methods are not used in any API but they can be used if specific aggregations are needed via separate APIs
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long>, SensorReadingRepositoryCustom {

    // Find all readings for a device in a timeframe
    //List<SensorReading> findByDeviceIdAndTsBetween(String deviceId, Instant start, Instant end);

    // Find all readings for a group in a timeframe
    //List<SensorReading> findByGroupIdAndTsBetween(String groupId, Instant start, Instant end);

    // Aggregate queries (example: avg, min, max) using JPQL
    @Query("SELECT AVG(r.reading) FROM SensorReading r WHERE r.deviceId = :deviceId AND r.ts BETWEEN :start AND :end")
    Double findAvgValueByDeviceId(String deviceId, Instant start, Instant end);

    @Query("SELECT MIN(r.reading) FROM SensorReading r WHERE r.deviceId = :deviceId AND r.ts BETWEEN :start AND :end")
    Double findMinValueByDeviceId(String deviceId, Instant start, Instant end);

    @Query("SELECT MAX(r.reading) FROM SensorReading r WHERE r.deviceId = :deviceId AND r.ts BETWEEN :start AND :end")
    Double findMaxValueByDeviceId(String deviceId, Instant start, Instant end);

    @Query(value = """
    SELECT 
        /*device_id AS deviceId,
        device_type AS deviceType,
        group_id AS groupId,
        metric,*/
        CASE WHEN :groupByDeviceId = true THEN device_id ELSE 'ALL' END AS deviceId,
        CASE WHEN :groupByDeviceType = true THEN device_type ELSE 'ALL' END AS deviceType,
        CASE WHEN :groupByGroupId = true THEN group_id ELSE 'ALL' END AS groupId,
        CASE WHEN :groupByMetric = true THEN metric ELSE 'ALL' END AS metric,
        AVG(reading) AS avgValue,
        MIN(reading) AS minValue,
        MAX(reading) AS maxValue,
        COUNT(*) AS countValue
    FROM SENSOR_READING
    WHERE ((:deviceId IS NULL OR :deviceId = '') OR device_id = :deviceId)
      AND ((:deviceType IS NULL OR :deviceType = '') OR device_type = :deviceType)
      AND ((:groupId IS NULL OR :groupId = '') OR group_id = :groupId)
      AND ((:metric IS NULL OR :metric = '') OR metric = :metric)
      AND ts BETWEEN :start AND :end
    GROUP BY
            CASE WHEN :groupByDeviceId = true THEN device_id ELSE 'ALL' END,
            CASE WHEN :groupByDeviceType = true THEN device_type ELSE 'ALL' END,
            CASE WHEN :groupByGroupId = true THEN group_id ELSE 'ALL' END,
            CASE WHEN :groupByMetric = true THEN metric ELSE 'ALL' END
    """, nativeQuery = true)
    List<Object[]> getAggregatesGrouped(
            @Param("deviceId") String deviceId,
            @Param("deviceType") String deviceType,
            @Param("groupId") String groupId,
            @Param("metric") String metric,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("groupByDeviceId") boolean groupByDeviceId,
            @Param("groupByDeviceType") boolean groupByDeviceType,
            @Param("groupByGroupId") boolean groupByGroupId,
            @Param("groupByMetric") boolean groupByMetric);
}
