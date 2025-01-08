package com.example.service;

import com.example.model.DataSourceConfig;
import com.example.model.ApiRecordConfig;
import com.example.model.PublishHistory;
import java.util.List;
import com.example.enums.GrayStage;

public interface ConfigService {
    // 数据源配置相关方法
    DataSourceConfig createDataSource(DataSourceConfig config);
    DataSourceConfig updateDataSource(String oldVersionId, DataSourceConfig newConfig);
    List<DataSourceConfig> getActiveDataSourceConfigs();
    
    // API记录配置相关方法
    ApiRecordConfig createApiRecord(ApiRecordConfig config);
    ApiRecordConfig updateApiRecord(String oldVersionId, ApiRecordConfig newConfig);
    List<ApiRecordConfig> getActiveApiRecordConfigs();
    
    // 发布相关方法
    void publishConfig(String versionId, String configType, List<String> grayGroups, String operator);
    void rollbackConfig(String currentVersionId, String targetVersionId, List<String> grayGroups, String operator);
    void deprecateConfig(String versionId, List<String> grayGroups, String operator);
    
    // 历史记录相关方法
    List<PublishHistory> getPublishHistory(String versionId);
    
    /**
     * 获取指定地域生效的所有数据源配置
     *
     * @param region 地域标识
     * @return 该地域生效的数据源配置列表
     */
    List<DataSourceConfig> getActiveDataSourceConfigsByRegion(String region);

    /**
     * 按灰度阶段发布配置
     *
     * @param versionId 配置版本号
     * @param configType 配置类型
     * @param stage 灰度阶段
     * @param operator 操作人
     */
    void publishConfigByStage(String versionId, String configType, GrayStage stage, String operator);

    /**
     * 获取指定source在指定地域生效的配置
     */
    DataSourceConfig getActiveDataSourceConfig(String source, String region);

    /**
     * 获取指定source的所有已发布配置
     */
    List<DataSourceConfig> getPublishedDataSourceConfigs(String source);
} 