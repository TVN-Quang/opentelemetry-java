package com.test.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tracing.jaeger")
public class AppProperties {
    private String endpoint;
    private String serviceName;
}
