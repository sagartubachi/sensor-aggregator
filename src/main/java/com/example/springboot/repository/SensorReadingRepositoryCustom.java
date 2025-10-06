package com.example.springboot.repository;

import com.example.springboot.bean.AggregateQueryParams;
import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.entity.SensorReading;

import java.util.List;
import java.util.Map;

public interface SensorReadingRepositoryCustom {
    public List<AggregateResponse> getDynamicAggregates(AggregateQueryParams params);
}
