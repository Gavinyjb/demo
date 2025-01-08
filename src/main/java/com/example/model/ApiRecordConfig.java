package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiRecordConfig implements ConfigIdentifier {
    private Long id;
    private String versionId;
    private String gatewayType;
    private String gatewayCode;
    private String apiVersion;
    private String apiName;
    private String loghubStream;
    private String basicConfig;
    private String eventConfig;
    private String userIdentityConfig;
    private String requestConfig;
    private String responseConfig;
    private String filterConfig;
    private String referenceResourceConfig;
    private String type;
    private String status;
    private String effectiveGrayGroups;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    @Override
    public String getIdentifier() {
        // 使用四个字段的组合作为唯一标识
        return String.format("%s:%s:%s:%s", 
            gatewayType, 
            gatewayCode, 
            apiVersion, 
            apiName);
    }

    /**
     * 判断两个配置是否为同一个API的配置
     */
    public boolean isSameApi(ApiRecordConfig other) {
        if (other == null) {
            return false;
        }
        return getIdentifier().equals(other.getIdentifier());
    }
} 