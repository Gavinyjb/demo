package com.example.model;

import lombok.Data;

@Data
public class DataSourceConfig extends BaseVersionedConfig {
    private String source;
    private String sourceGroup;
    private String gatewayType;
    private String dm;  // 数据|管控
    private String slsEndpoint;
    private String slsProject;
    private String slsLogstore;
    private String slsAccountId;
    private String slsAssumeRoleArn;
    private String slsCursor;
    private String consumeRegion;
    private String workerConfig;  // JSON格式: {fetchIntervalMillis, maxFetchLogGroupSize|actiontrail_work_id}

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