package com.team1.monew.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(3) // 최대 3번 시도
            .fixedBackoff(500) // 500ms 간격
            .retryOn(RuntimeException.class)
            .build();
    }
}
