package com.example.springboot.controller;

import com.example.springboot.bean.AggregateQueryParams;
import com.example.springboot.bean.AggregateResponse;
import com.example.springboot.exception.CustomException;
import com.example.springboot.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/*
This controller, accepts APIs to get aggregated readings form the system. There are separate parameters for filter and
group by. There also is support for multiple group by clauses to get data in a flexible manner.
 */
@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryService service;

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    public QueryController(QueryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> queryAggregates(@Valid AggregateQueryParams params) {

        // All the basic validations are happening via the @Valid checks by Spring. If anything is invalid,
        // the Global exception handler will transform it into a standard error response
        List<AggregateResponse> result = service.getAggregates(params);

        return ResponseEntity.ok(result);
    }
}
