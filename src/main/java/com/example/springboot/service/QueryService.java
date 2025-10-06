package com.example.springboot.service;

import com.example.springboot.bean.AggregateQueryParams;
import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.exception.CustomException;
import com.example.springboot.repository.SensorReadingRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/*
This service is called to get aggregate reading information from the system. The query is being constructed
dynamically so that it is extendable to all possible grouping scenarios. If the H2 database had support for
dynamic grouping using "When" which is supported in most relational databases, we could have used
the SensorReadingRepository::getAggregatesGrouped method for the same purpose. In production, we should use a
time series database for IOT reading ingestion use cases. I have used H2 for the prototype.
Also in production, we should ideally have integration with a Time Series Database which supports these type of
dynamic aggregations specifically as it stores dimensions as tags.
 */
@Service
@Transactional
public class QueryService {

    private EntityManager em;
    private final SensorReadingRepository repository;

    private static final Logger log = LoggerFactory.getLogger(QueryService.class);

    public QueryService(EntityManager em, SensorReadingRepository repository) {
        this.em = em;
        this.repository = repository;
    }

    public List<AggregateResponse> getAggregates(AggregateQueryParams params) {

        log.info("QueryService : getAggregates");

        // Executes the SensorReadingRepositoryImpl implementation
        List<AggregateResponse> result = repository.getDynamicAggregates(params);

        // Custom handling to provide a message instead of empty payload "[]"
        if (result.isEmpty()) {
            log.info("QueryController : No matching records found");
            throw new CustomException("No matching records found", CustomException.ErrorCode.NO_RECORDS_FOUND);
        }

        return result;
    }

    // If H2 supported conditional group by clauses, this implementation would be preferable as we are not constructing the SQL, Instead using conditional group by using When clause
    /*public List<AggregateResponse> getAggregates(String deviceId, String deviceType, String groupId, String metric,
                                                 Instant start, Instant end, boolean groupByDeviceId, boolean groupByDeviceType, boolean groupByGroupId, boolean groupByMetric) {
        return repository.getAggregatesGrouped(deviceId, deviceType, groupId, metric, start, end, groupByDeviceId, groupByDeviceType, groupByGroupId, groupByMetric)
                .stream()
                .map(arr -> new AggregateResponse(
                        (String) arr[0],                // deviceId
                        (String) arr[1],                // deviceType
                        (String) arr[2],                // groupId
                        (String) arr[3],                // metric
                        ((Number) arr[4]).doubleValue(),// avg
                        ((Number) arr[5]).doubleValue(),// min
                        ((Number) arr[6]).doubleValue(),// max
                        ((Number) arr[7]).longValue(),  // count
                        start,
                        end
                ))
                .collect(Collectors.toList());
    }*/
}