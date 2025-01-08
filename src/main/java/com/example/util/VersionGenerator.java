package com.example.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VersionGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger sequence = new AtomicInteger(1);

    public String generateDataSourceVersion() {
        return generateVersion("DS");
    }

    public String generateApiRecordVersion() {
        return generateVersion("AR");
    }

    private String generateVersion(String prefix) {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        String seq = String.format("%04d", sequence.getAndIncrement());
        return prefix + date + seq;
    }
} 