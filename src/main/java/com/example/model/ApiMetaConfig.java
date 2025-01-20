package com.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiMetaConfig extends BaseVersionedConfig {
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
     * 数据|管控
     */
    private String dm;
    
    /**
     * 网关编码
     */
    private String gatewayCode;
    
    /**
     * API版本
     */
    private String apiVersion;
    
    /**
     * 操作审计编码
     */
    private String actiontrailCode;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * API描述
     */
    private String description;
    
    /**
     * 可见性
     */
    private String visibility;
    
    /**
     * 隔离类型
     */
    private String isolationType;
    
    /**
     * 服务类型
     */
    private String serviceType;
    
    /**
     * 响应体日志
     */
    private Integer responseBodyLog;
    
    /**
     * 调用类型
     */
    private String invokeType;
    
    /**
     * 资源规格JSON
     */
    private String resourceSpec;
    
    /**
     * 生效标识
     */
    private String effectiveFlag;
    
    /**
     * 审计状态
     */
    private String auditStatus;

    @Override
    public String getIdentifier() {
        return String.format("%s:%s:%s:%s", 
            gatewayType, 
            gatewayCode, 
            apiVersion, 
            apiName);
    }
} 