package com.example.order_service.utils.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class Resili4jConfig {
    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(35)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .slidingWindowSize(1000)
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("order-serivce-circuitbreaker",circuitBreakerConfig);
        log.info("Order Service circuit breaker open: "+circuitBreaker.getName());
        return circuitBreaker;
    }

    @Bean
    public Retry retry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(500))
                .build();
        return Retry.of("order-service-retry", retryConfig);
    }
}
