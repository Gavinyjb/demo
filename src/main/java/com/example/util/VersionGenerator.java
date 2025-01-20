package com.example.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VersionGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger dataSourceCounter = new AtomicInteger(1);
    private final AtomicInteger apiRecordCounter = new AtomicInteger(1);
    private final AtomicInteger apiMetaCounter = new AtomicInteger(1);
    private String currentDate = LocalDateTime.now().format(DATE_FORMATTER);

    /**
     * 生成数据源配置版本号
     * 格式：DS + 年月日 + 5位序号，如：DS2024011500001
     */
    public String generateDataSourceVersion() {
        return generateVersion("DS", dataSourceCounter);
    }

    /**
     * 生成API记录配置版本号
     * 格式：AR + 年月日 + 5位序号，如：AR2024011500001
     */
    public String generateApiRecordVersion() {
        return generateVersion("AR", apiRecordCounter);
    }

    /**
     * 生成API Meta配置版本号
     * 格式：AM + 年月日 + 5位序号，如：AM2024011500001
     */
    public String generateApiMetaVersion() {
        return generateVersion("AM", apiMetaCounter);
    }

    private String generateVersion(String prefix, AtomicInteger counter) {
        String today = LocalDateTime.now().format(DATE_FORMATTER);
        if (!today.equals(currentDate)) {
            currentDate = today;
            counter.set(1);
        }
        return String.format("%s%s%05d", prefix, today, counter.getAndIncrement());
    }
} 