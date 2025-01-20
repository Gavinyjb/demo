package com.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiRecordConfig extends BaseVersionedConfig {
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
     * 基础配置JSON
     */
    private String basicConfig;
    
    /**
     * 事件配置JSON
     */
    private String eventConfig;
    
    /**
     * 用户身份配置JSON
     */
    private String userIdentityConfig;
    
    /**
     * 请求配置JSON
     */
    private String requestConfig;
    
    /**
     * 响应配置JSON
     */
    private String responseConfig;
    
    /**
     * 过滤配置JSON
     */
    private String filterConfig;
    
    /**
     * 引用资源配置JSON
     */
    private String referenceResourceConfig;

    @Override
    public String getIdentifier() {
        return String.format("%s:%s:%s:%s", 
            gatewayType, 
            gatewayCode, 
            apiVersion, 
            apiName);
    }

    public boolean isSameApi(ApiRecordConfig other) {
        if (other == null) {
            return false;
        }
        return getIdentifier().equals(other.getIdentifier());
    }
} 