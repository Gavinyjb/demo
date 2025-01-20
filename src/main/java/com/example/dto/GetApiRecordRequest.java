package com.example.dto;

import lombok.Data;

@Data
public class GetApiRecordRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * 网关类型
     */
    private String gatewayType;
    
    /**
     * 网关编码
     */
    private String gatewayCode;
    
    /**
     * API版本
     */
    private String apiVersion;
    
    /**
     * API名称
     */
    private String apiName;
    
    /**
     * 地域
     */
    private String region;
} 