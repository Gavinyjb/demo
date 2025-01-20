package com.example.dto;

import lombok.Data;

@Data
public class GetConfigRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 数据源名称
     */
    private String name;
    
    /**
     * 地域
     */
    private String region;
} 