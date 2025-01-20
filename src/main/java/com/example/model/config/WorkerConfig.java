package com.example.model.config;

import lombok.Data;

@Data
public class WorkerConfig {
    /**
     * 拉取间隔（毫秒）
     */
    private Integer fetchIntervalMillis;
    
    /**
     * 最大拉取日志组大小
     */
    private Integer maxFetchLogGroupSize;
    
    /**
     * ActionTrail工作ID
     */
    private String actiontrailWorkId;
} 