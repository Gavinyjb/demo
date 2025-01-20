package com.example.dto;

import lombok.Data;

@Data
public class GetPublishHistoryRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 灰度阶段
     */
    private String stage;
} 