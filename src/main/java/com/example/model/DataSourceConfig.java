package com.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceConfig extends BaseVersionedConfig {
    /**
     * 数据源标识
     */
    private String source;
    
    /**
     * 数据源分组
     */
    private String sourceGroup;
    
    /**
     * 网关类型
     */
    private String gatewayType;
    
    /**
     * 数据|管控
     */
    private String dm;
    
    /**
     * SLS访问地址
     */
    private String slsEndpoint;
    
    /**
     * SLS项目
     */
    private String slsProject;
    
    /**
     * SLS日志库
     */
    private String slsLogstore;
    
    /**
     * SLS账号ID
     */
    private String slsAccountId;
    
    /**
     * SLS角色ARN
     */
    private String slsAssumeRoleArn;
    
    /**
     * SLS游标
     */
    private String slsCursor;
    
    /**
     * 消费地域
     */
    private String consumeRegion;
    
    /**
     * 工作配置JSON
     */
    private String workerConfig; // JSON格式: {fetchIntervalMillis, maxFetchLogGroupSize|actiontrail_work_id}

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