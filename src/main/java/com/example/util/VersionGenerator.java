package com.example.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VersionGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int RANDOM_BOUND = 1000; // 随机数范围：0-999
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final int MAX_SEQUENCE = 9999;
    
    private static volatile String lastTimestamp = "";
    private static final Object lock = new Object();

    /**
     * 生成数据源配置版本号
     * 格式：DS + 时间戳(14位) + 随机数(3位) + 序列号(4位)
     * 示例：DS202401211545230019999
     */
    public String generateDataSourceVersion() {
        return "DS" + generateVersionSuffix();
    }

    /**
     * 生成API元数据配置版本号
     * 格式：API + 时间戳(14位) + 随机数(3位) + 序列号(4位)
     * 示例：API202401211545230019999
     */
    public String generateApiMetaVersion() {
        return "API" + generateVersionSuffix();
    }

    /**
     * 生成版本号后缀
     * 格式：时间戳(14位) + 随机数(3位) + 序列号(4位)
     */
    private String generateVersionSuffix() {
        String currentTimestamp = LocalDateTime.now().format(DATE_FORMATTER);
        int randomNum = ThreadLocalRandom.current().nextInt(RANDOM_BOUND);
        
        synchronized (lock) {
            // 如果时间戳变化，重置序列号
            if (!currentTimestamp.equals(lastTimestamp)) {
                SEQUENCE.set(0);
                lastTimestamp = currentTimestamp;
            }
            
            // 获取并递增序列号
            int sequence = SEQUENCE.getAndIncrement();
            if (sequence > MAX_SEQUENCE) {
                // 如果序列号超出范围，等待到下一毫秒
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return generateVersionSuffix();
            }
            
            return String.format("%s%03d%04d", 
                currentTimestamp,
                randomNum,
                sequence
            );
        }
    }
} 