package com.example.springboot.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("execution(* com.example.springboot.service..*(..))")  // Adjust package as needed
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering: {}", methodName);

        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        log.info("Execution time of {} :: {} ms", methodName, duration);

        log.info("Exiting: {}\n", methodName);
        return proceed;
    }
}
