package com.test.servicea;
import com.test.constants.AppConstants;
import com.test.config.AppProperties;
import com.test.tracing.Traceable;
// opentelemetry import
// import io.opentelemetry.api.GlobalOpenTelemetry;
// import io.opentelemetry.api.metrics.LongCounter;
// import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
// spring import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.test", exclude = {SecurityAutoConfiguration.class})
@EnableAspectJAutoProxy
@RestController
public class ServiceAApplication {

    // Khởi tạo Tracer, Meter và Counter từ OpenTelemetry
    // private static final Tracer tracer = GlobalOpenTelemetry.getTracer("serviceATracer");
    // private static final Meter meter = GlobalOpenTelemetry.getMeter("serviceAMeter");
    // private static final LongCounter requestCounter = meter
    //         .counterBuilder("service_a_requests_total")
    //         .setDescription("Tổng số request đến Service A")
    //         .build();
    private final ServiceAApplication self;
    private final Tracer tracer;
    private final RestTemplate restTemplate;

    public ServiceAApplication(@Lazy ServiceAApplication self,Tracer tracer, RestTemplate restTemplate) {
        this.tracer = tracer;
        this.self = self;
        this.restTemplate = restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @GetMapping("/api/service-a")
    @Traceable
    public String serviceA() {
        // Tăng counter metrics
        // requestCounter.add(1);
        // Tạo span để theo dõi trace của Service A
        try {
            System.out.println("Service A: Nhận request, gọi Service B...");
            String responseB = self.funcF();
          
            return "Service A response, got from Service B: " + responseB;
        } catch (Exception e) {
            throw e;
        } 
    }

    @Traceable
    public String funcF() {
        System.out.println("answer");
          // Gọi Service B qua DNS nội bộ trên k8s (tên service: service-b)
            String serviceBUrl = "http://localhost:8081/api/service-b";
            String responseB = restTemplate.getForObject(serviceBUrl, String.class);
            return responseB;
    }
}