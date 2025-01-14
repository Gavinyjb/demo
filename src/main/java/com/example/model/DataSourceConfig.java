package com.example.model;

import lombok.Data;

@Data
public class DataSourceConfig extends BaseVersionedConfig {
    private Long atWorkId;
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