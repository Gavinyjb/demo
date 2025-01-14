package com.example.model;

import lombok.Data;

@Data
public class ApiMetaConfig extends BaseVersionedConfig {
    private String apiName;
    private String product;
    private String gatewayType;
    private String dm;
    private String gatewayCode;
    private String apiVersion;
    private String actiontrailCode;
    private String operationType;
    private String description;
    private String visibility;
    private String isolationType;
    private String serviceType;
    private Boolean responseBodyLog;
    private String invokeType;
    private String resourceSpec;
    private String effectiveFlag;
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