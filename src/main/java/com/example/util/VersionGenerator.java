package com.example.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VersionGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    
    // 每个配置类型使用独立的序列号
    private final AtomicInteger dataSourceCounter = new AtomicInteger(0);
    private final AtomicInteger apiRecordCounter = new AtomicInteger(0);
    private final AtomicInteger apiMetaCounter = new AtomicInteger(0);
    
    // 记录上一次生成的时间戳
    private volatile String lastTimestamp = generateTimestamp();

    /**
     * 生成数据源配置版本号
     * 格式：DS + 年月日 + 时分秒 + 3位序号，如：DS20240115123456001
     */
    public synchronized String generateDataSourceVersion() {
        return generateVersion("DS", dataSourceCounter);
    }

    /**
     * 生成API记录配置版本号
     */
    public synchronized String generateApiRecordVersion() {
        return generateVersion("AR", apiRecordCounter);
    }

    /**
     * 生成API Meta配置版本号
     */
    public synchronized String generateApiMetaVersion() {
        return generateVersion("AM", apiMetaCounter);
    }

    private String generateVersion(String prefix, AtomicInteger counter) {
        String currentTimestamp = generateTimestamp();
        
        // 如果时间戳发生变化，重置计数器
        if (!currentTimestamp.equals(lastTimestamp)) {
            counter.set(0);
            lastTimestamp = currentTimestamp;
        }
        
        // 序号达到最大值时重置
        if (counter.get() >= 999) {
            counter.set(0);
        }
        
        return String.format("%s%s%03d", 
            prefix, 
            currentTimestamp,
            counter.incrementAndGet()
        );
    }
    
    private String generateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATE_FORMATTER) + now.format(TIME_FORMATTER);
    }
} 