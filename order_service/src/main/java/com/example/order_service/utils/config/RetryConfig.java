package com.example.order_service.utils.config;

import feign.Retryer;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetryConfig {
    @Bean
    public Retry retry() {
        io.github.resilience4j.retry.RetryConfig retryConfig = io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(5000))
                .build();
        return Retry.of("order-service-retry", retryConfig);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(500,2000,3);
    }
}
