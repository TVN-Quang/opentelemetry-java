package com.test.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

@Aspect
@Component
public class TracingAspect {
    private final Tracer tracer;

    public TracingAspect(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("service-a");
    }

    @Around("@annotation(com.test.tracing.Traceable)")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String spanName = getSpanName(joinPoint);
        Span span = tracer.spanBuilder(spanName).setParent(Context.current()).startSpan();
        try (Scope scope = span.makeCurrent()) {
            return joinPoint.proceed();
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private String getSpanName(ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);

        // Kiểm tra @GetMapping trước
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return "GET " + getMapping.value()[0]; // Format: "GET /api/service-a"
        }


        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        
        // Nếu phương thức có annotation @RequestMapping
        if (requestMapping != null) {
            // Lấy HTTP method (mặc định là GET nếu không có method được chỉ định)
            String httpMethod = "GET";
            if (requestMapping.method().length > 0) {
                httpMethod = requestMapping.method()[0].name();
            }

            // Lấy path (mặc định là "/" nếu không có path được chỉ định)
            String path = "/";
            if (requestMapping.value().length > 0) {
                path = requestMapping.value()[0];
            }

            return httpMethod + " " + path; // Format: "GET /api/service-a"
        }

        String className = joinPoint.getSignature().getDeclaringTypeName(); // Lấy tên lớp thực tế
        String methodName = method.getName(); // Lấy tên phương thức
        return className + "." + methodName;
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getMethod();
        } else {
            throw new IllegalArgumentException("JoinPoint is not a method signature: " + joinPoint.getSignature());
        }
    }
}