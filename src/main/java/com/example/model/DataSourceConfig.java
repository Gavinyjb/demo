package com.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataSourceConfig implements ConfigIdentifier {
    private Long id;
    private String versionId;
    private String source;
    private String sourceGroup;
    private String gatewayType;
    private String dm;
    private String loghubEndpoint;
    private String loghubProject;
    private String loghubStream;
    private String loghubAccesskeyId;
    private String loghubAccesskeySecret;
    private String loghubAssumeRoleArn;
    private String loghubCursor;
    private String consumeRegion;
    private Integer dataFetchIntervalMillis;
    private String status;
    private String effectiveGrayGroups;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    @Override
    public String getIdentifier() {
        return this.source;
    }

    public boolean isSameSource(DataSourceConfig other) {
        if (other == null) {
            return false;
        }
        return getIdentifier().equals(other.getIdentifier());
    }
} 