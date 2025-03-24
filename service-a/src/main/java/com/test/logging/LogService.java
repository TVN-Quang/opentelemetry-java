package com.test.logging;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import org.springframework.stereotype.Service;
import io.opentelemetry.api.logs.Severity;

import java.time.Instant;

@Service
public class LogService {
    private final Logger logger;

    public LogService(Logger logger) {
        this.logger = logger;
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String severity, String message) {
        Severity severityEnum;
    try {
        severityEnum = Severity.valueOf(severity.toUpperCase());
    } catch (IllegalArgumentException e) {
        // Xử lý trường hợp giá trị severity không hợp lệ, có thể đặt mặc định hoặc ghi log lỗi
        severityEnum = Severity.UNDEFINED_SEVERITY_NUMBER; // Hoặc một giá trị mặc định khác
    }
         System.out.println(message);
        LogRecordBuilder logRecord = logger.logRecordBuilder()
            .setBody(message)
            .setSeverity(severityEnum)
            .setTimestamp(Instant.now());

    logRecord.emit(); // Gửi log
    }
}
