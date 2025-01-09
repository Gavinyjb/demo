package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiMetaConfig implements ConfigIdentifier {
    private Long id;
    private String versionId;
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
    private String status;
    private String effectiveGrayGroups;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private String effectiveFlag;
    private String auditStatus;

    @Override
    public String getIdentifier() {
        // 使用四个字段的组合作为唯一标识
        return String.format("%s:%s:%s:%s", 
            gatewayType, 
            gatewayCode, 
            apiVersion, 
            apiName);
    }
} 