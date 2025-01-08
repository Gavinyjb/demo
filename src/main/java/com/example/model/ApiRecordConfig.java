package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiRecordConfig {
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
} 