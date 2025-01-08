package com.example.service.impl;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.mapper.DataSourceConfigMapper;
import com.example.mapper.ApiRecordConfigMapper;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.DataSourceConfig;
import com.example.model.ApiRecordConfig;
import com.example.model.PublishHistory;
import com.example.service.ConfigService;
import com.example.util.VersionGenerator;
import com.example.util.RegionProvider;
import com.example.enums.GrayStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Autowired
    private ApiRecordConfigMapper apiRecordConfigMapper;
    
    @Autowired
    private PublishHistoryMapper publishHistoryMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;

    @Autowired
    private RegionProvider regionProvider;

    @Override
    @Transactional
    public DataSourceConfig createDataSource(DataSourceConfig config) {
        config.setVersionId(versionGenerator.generateDataSourceVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public void publishConfig(String versionId, String configType, List<String> grayGroups, String operator) {
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            // 获取要发布的配置
            DataSourceConfig config = dataSourceConfigMapper.findByVersionId(versionId);
            if (config == null) {
                throw new RuntimeException("Configuration not found: " + versionId);
            }

            // 获取同source的已发布配置
            List<DataSourceConfig> publishedConfigs = 
                dataSourceConfigMapper.findPublishedConfigsBySource(config.getSource());

            // 如果已发布配置超过2个，将最老的标记为DEPRECATED
            if (publishedConfigs.size() >= 2) {
                DataSourceConfig oldestConfig = publishedConfigs.get(publishedConfigs.size() - 1);
                dataSourceConfigMapper.updateStatus(oldestConfig.getVersionId(), 
                    ConfigStatus.DEPRECATED.name(), null);
            }
        }
        
        // 执行发布操作
        String grayGroupsJson = String.join(",", grayGroups);
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            dataSourceConfigMapper.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        } else if (ConfigType.API_RECORD.name().equals(configType)) {
            apiRecordConfigMapper.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        }

        // 记录发布历史
        recordHistory(versionId, configType, ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
    }

    @Override
    @Transactional
    public DataSourceConfig updateDataSource(String oldVersionId, DataSourceConfig newConfig) {
        DataSourceConfig oldConfig = dataSourceConfigMapper.findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateDataSourceVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(newConfig);
        return newConfig;
    }

    @Override
    public List<DataSourceConfig> getActiveDataSourceConfigs() {
        return dataSourceConfigMapper.findAllPublished();
    }

    @Override
    @Transactional
    public ApiRecordConfig createApiRecord(ApiRecordConfig config) {
        config.setVersionId(versionGenerator.generateApiRecordVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public ApiRecordConfig updateApiRecord(String oldVersionId, ApiRecordConfig newConfig) {
        ApiRecordConfig oldConfig = apiRecordConfigMapper.findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateApiRecordVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(newConfig);
        return newConfig;
    }

    @Override
    public List<ApiRecordConfig> getActiveApiRecordConfigs() {
        return apiRecordConfigMapper.findAllPublished();
    }

    @Override
    @Transactional
    public void rollbackConfig(String currentVersionId, String targetVersionId, List<String> grayGroups, String operator) {
        String grayGroupsJson = String.join(",", grayGroups);
        
        // 获取目标版本配置
        DataSourceConfig targetDataSource = dataSourceConfigMapper.findByVersionId(targetVersionId);
        ApiRecordConfig targetApiRecord = apiRecordConfigMapper.findByVersionId(targetVersionId);
        
        if (targetDataSource != null) {
            // 数据源配置回滚
            dataSourceConfigMapper.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
            dataSourceConfigMapper.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
            recordHistory(targetVersionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
        } else if (targetApiRecord != null) {
            // API记录配置回滚
            apiRecordConfigMapper.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
            apiRecordConfigMapper.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
            recordHistory(targetVersionId, ConfigType.API_RECORD.name(), ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
        }
    }

    @Override
    @Transactional
    public void deprecateConfig(String versionId, List<String> grayGroups, String operator) {
        String grayGroupsJson = String.join(",", grayGroups);
        
        DataSourceConfig dataSource = dataSourceConfigMapper.findByVersionId(versionId);
        if (dataSource != null) {
            dataSourceConfigMapper.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), grayGroupsJson);
            recordHistory(versionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.DEPRECATED.name(), grayGroupsJson, operator);
        }
        
        ApiRecordConfig apiRecord = apiRecordConfigMapper.findByVersionId(versionId);
        if (apiRecord != null) {
            apiRecordConfigMapper.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), grayGroupsJson);
            recordHistory(versionId, ConfigType.API_RECORD.name(), ConfigStatus.DEPRECATED.name(), grayGroupsJson, operator);
        }
    }

    @Override
    public List<PublishHistory> getPublishHistory(String versionId) {
        return publishHistoryMapper.findByVersionId(versionId);
    }

    @Override
    public List<DataSourceConfig> getActiveDataSourceConfigsByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findByRegion(region);
    }

    @Override
    @Transactional
    public void publishConfigByStage(String versionId, String configType, GrayStage stage, String operator) {
        List<String> grayGroups = stage.getRegions();
        publishConfig(versionId, configType, grayGroups, operator);
    }

    @Override
    public DataSourceConfig getActiveDataSourceConfig(String source, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findActiveConfigBySourceAndRegion(source, region);
    }

    @Override
    public List<DataSourceConfig> getPublishedDataSourceConfigs(String source) {
        return dataSourceConfigMapper.findPublishedConfigsBySource(source);
    }

    private void recordHistory(String versionId, String configType, String status, String grayGroups, String operator) {
        PublishHistory history = new PublishHistory();
        history.setVersionId(versionId);
        history.setConfigType(configType);
        history.setStatus(status);
        history.setGrayGroups(grayGroups);
        history.setOperator(operator);
        publishHistoryMapper.insert(history);
    }
} 