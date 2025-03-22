package com.test.serviceb;
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
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
// spring import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestHeader;

@SpringBootApplication(scanBasePackages = "com.test", exclude = {SecurityAutoConfiguration.class})
@EnableAspectJAutoProxy
@RestController
public class ServiceBApplication {

    // Khởi tạo Tracer, Meter và Counter từ OpenTelemetry
    // private static final Tracer tracer = GlobalOpenTelemetry.getTracer("serviceATracer");
    // private static final Meter meter = GlobalOpenTelemetry.getMeter("serviceAMeter");
    // private static final LongCounter requestCounter = meter
    //         .counterBuilder("service_a_requests_total")
    //         .setDescription("Tổng số request đến Service A")
    //         .build();
    private final ServiceBApplication self;
    private final Tracer tracer;
    private final RestTemplate restTemplate = new RestTemplate();
    private final TextMapPropagator propagator;

    public ServiceBApplication(@Lazy ServiceBApplication self, Tracer tracer, TextMapPropagator propagator) {
        this.tracer = tracer;
        this.self = self;
        this.propagator = propagator;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServiceBApplication.class, args);
    }

    @GetMapping("/api/service-b")
    @Traceable
    public String serviceB(@RequestHeader Map<String, String> headers) {
        self.funcF();
        return "Hello from Service B!";
    }

    @Traceable
    public void funcF() {
        System.out.println("answer");
    }
    
}