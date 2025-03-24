package com.test.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;

import io.opentelemetry.semconv.ServiceAttributes;

@Configuration
public class OpenTelemetryConfig {

    private final AppProperties appProperties;
    
    public OpenTelemetryConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetryConfig.init(appProperties);
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("service-a"); // Tạo và trả về bean Tracer
    }

    @Bean
    public TextMapPropagator textMapPropagator(OpenTelemetry openTelemetry) {
        return openTelemetry.getPropagators().getTextMapPropagator();
    }

    @Bean
    public Logger logger(OpenTelemetry openTelemetry) {
        // Sử dụng LogsBridge để lấy logger
        return openTelemetry.getLogsBridge().get("service-a");
    }

    private static OpenTelemetry init(AppProperties appProperties) {
        // Định nghĩa Resource với tên service cụ thể
        Resource resource = Resource.getDefault().toBuilder()
            .put(ServiceAttributes.SERVICE_NAME, "service-a")
            .build();
        // Tạo Jaeger Exporter
        OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(appProperties.getEndpoint()) // Jaeger gRPC endpoint
                .build();

        // Cấu hình SdkTracerProvider với Jaeger Exporter
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(SimpleSpanProcessor.create(otlpExporter)) // Thêm exporter vào span processor
                .build();
        
        OtlpGrpcLogRecordExporter otlpLogExporter = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(appProperties.getEndpoint())
                .build();

         // Cấu hình Logger Provider
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(otlpLogExporter).build())
                .build();

        // Thiết lập OpenTelemetry SDK
        return OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(
                        TextMapPropagator.composite(
                                W3CTraceContextPropagator.getInstance(),
                                W3CBaggagePropagator.getInstance()
                        )))
                .setLoggerProvider(loggerProvider)
                .setTracerProvider(tracerProvider)
                .build();
    }
}
