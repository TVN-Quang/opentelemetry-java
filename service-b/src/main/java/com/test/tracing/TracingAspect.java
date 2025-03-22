package com.test.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

@Aspect
@Component
public class TracingAspect {
    private final Tracer tracer;
    private final TextMapPropagator propagator;

    public TracingAspect(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("service-b");
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }

    @Around("@annotation(com.test.tracing.Traceable)")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // ✅ Lấy request headers nếu có
        Map<String, String> headers = getRequestHeaders(joinPoint);

        // ✅ Extract context từ headers
        Context extractedContext = propagator.extract(
            Context.current(), headers, new TextMapGetter<Map<String, String>>() {
                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    return carrier.get(key);
                }
            }
        );

        // ✅ Tạo span mới với context đã extract
        String spanName = getSpanName(joinPoint);
        Span span = tracer.spanBuilder(spanName).setParent(extractedContext).startSpan();

        try (Scope scope = span.makeCurrent()) {
            return joinPoint.proceed();
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /** Hàm tìm `@RequestHeader` trong danh sách tham số của hàm được gọi */
    private Map<String, String> getRequestHeaders(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestHeader.class) && args[i] instanceof Map) {
                return (Map<String, String>) args[i];
            }
        }
        return Map.of(); // Trả về map rỗng nếu không có request headers
    }

    private String getSpanName(ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return "GET " + getMapping.value()[0]; 
        }

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            String httpMethod = requestMapping.method().length > 0
                    ? requestMapping.method()[0].name()
                    : "GET";
            String path = requestMapping.value().length > 0
                    ? requestMapping.value()[0]
                    : "/";
            return httpMethod + " " + path;
        }

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = method.getName();
        return className + "." + methodName;
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature) {
            return ((MethodSignature) joinPoint.getSignature()).getMethod();
        }
        throw new IllegalArgumentException("JoinPoint is not a method signature: " + joinPoint.getSignature());
    }
}
