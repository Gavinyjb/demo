package com.example.service;

import com.example.model.DataSourceConfig;
import java.util.List;

/**
 * 数据源配置服务接口
 */
public interface DataSourceConfigService {
    /**
     * 创建数据源配置
     */
    DataSourceConfig create(DataSourceConfig config);
    
    /**
     * 更新数据源配置
     */
    DataSourceConfig update(String oldVersionId, DataSourceConfig newConfig);
    
    /**
     * 获取所有已发布的配置
     */
    List<DataSourceConfig> getAllPublished();
    
    /**
     * 获取指定地域生效的配置
     */
    List<DataSourceConfig> getActiveByRegion(String region);
    
    /**
     * 获取指定source在指定地域生效的配置
     */
    DataSourceConfig getActiveBySourceAndRegion(String source, String region);
    
    /**
     * 获取指定source的所有已发布配置
     */
    List<DataSourceConfig> getPublishedBySource(String source);
    
    /**
     * 更新配置状态
     */
    void updateStatus(String versionId, String status, String grayGroups);
} 