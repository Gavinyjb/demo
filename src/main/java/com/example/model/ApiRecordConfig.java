package com.example.model;

import lombok.Data;

@Data
public class ApiRecordConfig extends BaseVersionedConfig {
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