package com.example.dto;

import lombok.Data;

@Data
public class GetApiMetaRequest {
    /**
     * 版本ID
     */
    private String versionId;
    
    /**
     * API名称
     */
    private String apiName;
    
    /**
     * 产品名称
     */
    private String product;
    
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
     * 地域
     */
    private String region;
} 