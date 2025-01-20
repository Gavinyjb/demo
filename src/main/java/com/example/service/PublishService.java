package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.BaseVersionedConfig;
import com.example.model.PublishHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 配置发布服务
 */
@Service
public class PublishService {
    
    @Autowired
    private DataSourceConfigService dataSourceConfigService;
    
    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @Autowired
    private ApiMetaConfigService apiMetaConfigService;
    @Autowired
    private PublishHistoryMapper publishHistoryMapper;

    @Autowired
    private Map<String, BaseConfigService<?>> configServices;

    /**
     * 发布配置
     */
    @Transactional
    public void publishConfig(String versionId, String configType, String stage, String operator) {
        BaseConfigService<?> service = getConfigService(configType);
        service.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), stage);
        
        // 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(versionId)
            .configType(configType)
            .configStatus(ConfigStatus.PUBLISHED.name())
            .stage(stage)
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
    }

    /**
     * 废弃配置
     */
    @Transactional
    public void deprecateConfig(String versionId, String configType, String operator) {
        BaseConfigService<?> service = getConfigService(configType);
        service.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), null);
        
        // 记录废弃历史
        PublishHistory history = PublishHistory.builder()
            .versionId(versionId)
            .configType(configType)
            .configStatus(ConfigStatus.DEPRECATED.name())
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
    }

    /**
     * 回滚配置
     */
    @Transactional
    public void rollback(String currentVersionId, String targetVersionId, String operator) {
        // 获取配置类型
        PublishHistory currentHistory = publishHistoryMapper.findByVersionId(currentVersionId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Current version not found"));

        // 废弃当前版本
        deprecateConfig(currentVersionId, currentHistory.getConfigType(), operator);

        // 重新发布目标版本
        PublishHistory targetHistory = publishHistoryMapper.findByVersionId(targetVersionId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Target version not found"));

        publishConfig(targetVersionId, targetHistory.getConfigType(), targetHistory.getStage(), operator);
    }

    /**
     * 回滚到上一个版本
     */
    @Transactional
    public void rollbackToPrevious(String identifier, String configType, String operator) {
        List<? extends BaseVersionedConfig> publishedConfigs = getPublishedConfigs(identifier, configType);
        
        if (publishedConfigs.size() < 2) {
            throw new RuntimeException("No previous version to rollback for: " + identifier);
        }
        
        BaseVersionedConfig currentConfig = publishedConfigs.get(0);
        BaseVersionedConfig previousConfig = publishedConfigs.get(1);
        
        rollback(currentConfig.getVersionId(), previousConfig.getVersionId(), operator);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator) {
        List<? extends BaseVersionedConfig> publishedConfigs = getPublishedConfigs(identifier, configType);
        
        if (publishedConfigs.isEmpty()) {
            throw new RuntimeException("No active version for: " + identifier);
        }
        
        rollback(publishedConfigs.get(0).getVersionId(), targetVersionId, operator);
    }

    private String getConfigTypeFromVersionId(String versionId) {
        if (versionId == null || versionId.length() < 2) {
            throw new IllegalArgumentException("Invalid version ID: " + versionId);
        }
        
        String prefix = versionId.substring(0, 2);
        switch (prefix) {
            case "DS":
                return ConfigType.DATA_SOURCE.name();
            case "AR":
                return ConfigType.API_RECORD.name();
            case "AM":
                return ConfigType.API_META.name();
            default:
                throw new IllegalArgumentException("Unknown version ID prefix: " + prefix);
        }
    }

    private void updateConfigStatus(String versionId, String status, String grayGroups, String configType) {
        switch (ConfigType.valueOf(configType)) {
            case DATA_SOURCE:
                dataSourceConfigService.updateStatus(versionId, status, grayGroups);
                break;
            case API_RECORD:
                apiRecordConfigService.updateStatus(versionId, status, grayGroups);
                break;
            case API_META:
                apiMetaConfigService.updateStatus(versionId, status, grayGroups);
                break;
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    private List<? extends BaseVersionedConfig> getPublishedConfigs(String identifier, String configType) {
        BaseConfigService<?> configService = getConfigService(configType);
        return configService.getPublishedByIdentifier(identifier);
    }

    /**
     * 获取发布历史
     */
    public List<PublishHistory> getPublishHistory(String versionId) {
        return publishHistoryMapper.findByVersionId(versionId);
    }

    private BaseConfigService<?> getConfigService(String configType) {
        switch (configType) {
            case "API_META":
                return apiMetaConfigService;
            case "API_RECORD":
                return apiRecordConfigService;
            case "DATA_SOURCE":
                return dataSourceConfigService;
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    @Transactional
    public void publishByStage(String versionId, String configType, GrayStage stage, String operator) {
        publishConfig(versionId, configType, stage.name(), operator);
    }

    /**
     * 获取指定配置类型和灰度阶段的发布历史
     */
    public List<PublishHistory> getHistoryByTypeAndStage(String configType, String stage) {
        return publishHistoryMapper.findByConfigTypeAndStage(configType, stage);
    }
} 