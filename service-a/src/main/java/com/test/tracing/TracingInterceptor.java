package com.test.tracing;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TracingInterceptor implements ClientHttpRequestInterceptor {

    private final TextMapPropagator propagator;

    public TracingInterceptor() {
        this.propagator = W3CTraceContextPropagator.getInstance();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Lấy context hiện tại (KHÔNG tạo span mới)
        Context currentContext = Context.current();

        // Debug: Kiểm tra context trước khi inject
        System.out.println("Current Context Before Inject: " + currentContext);

        // Inject trace context vào headers của request
        propagator.inject(currentContext, request.getHeaders(), HttpHeaders::add);

        // Debug: Kiểm tra headers sau khi inject
        System.out.println("Headers after inject:");
        request.getHeaders().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });

        return execution.execute(request, body);
    }
}
