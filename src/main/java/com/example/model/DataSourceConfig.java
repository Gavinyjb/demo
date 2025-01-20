package com.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceConfig extends BaseVersionedConfig {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 数据源名称
     */
    private String name;
    
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
     * SLS RegionId
     */
    private String slsRegionId;
    
    /**
     * SLS Endpoint
     */
    private String slsEndpoint;
    
    /**
     * SLS Project
     */
    private String slsProject;
    
    /**
     * SLS LogStore
     */
    private String slsLogStore;
    
    /**
     * SLS 所属账号
     */
    private String slsAccountId;
    
    /**
     * 拉取日志的 SLS 角色
     */
    private String slsRoleArn;
    
    /**
     * SLS游标
     */
    private String slsCursor;
    
    /**
     * 消费地域
     */
    private String consumeRegion;
    
    /**
     * 消费组名称
     */
    private String consumerGroupName;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 消费配置
     */
    private String workerConfig;
    
    /**
     * 备注
     */
    private String comment;

    @Override
    public String getIdentifier() {
        return this.name;
    }

    public boolean isSameSource(DataSourceConfig other) {
        if (other == null) {
            return false;
        }
        return getIdentifier().equals(other.getIdentifier());
    }
} 