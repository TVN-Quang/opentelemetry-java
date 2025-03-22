package com.test.config;
import com.test.tracing.TracingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    private final TracingInterceptor tracingInterceptor;

    public RestTemplateConfig(TracingInterceptor tracingInterceptor) {
        this.tracingInterceptor = tracingInterceptor;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .interceptors(List.of(tracingInterceptor)) // Thêm interceptor vào RestTemplate
                .build();
    }
}
