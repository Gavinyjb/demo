package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.ConfigIdentifier;
import com.example.model.PublishHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 发布配置
     */
    @Transactional
    public void publish(String versionId, String configType, List<String> grayGroups, String operator) {
        // 如果是全量发布，使用 "all"
        String grayGroupsJson = grayGroups.size() == 1 && grayGroups.get(0).equals("all") 
            ? "all" 
            : String.join(",", grayGroups);
        
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            dataSourceConfigService.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        } else if (ConfigType.API_RECORD.name().equals(configType)) {
            apiRecordConfigService.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        } else if (ConfigType.API_META.name().equals(configType)) {
            apiMetaConfigService.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        }
        
        recordHistory(versionId, configType, ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
    }

    /**
     * 按阶段发布配置
     */
    @Transactional
    public void publishByStage(String versionId, String configType, GrayStage stage, String operator) {
        publish(versionId, configType, stage.getRegions(), operator);
    }

    /**
     * 废弃配置
     */
    @Transactional
    public void deprecate(String versionId, String operator) {
        // 根据版本号前缀确定配置类型
        String configType = getConfigTypeFromVersionId(versionId);
        
        // 废弃配置
        updateConfigStatus(versionId, ConfigStatus.DEPRECATED.name(), "", configType);
        
        // 记录发布历史
        recordHistory(versionId, configType, ConfigStatus.DEPRECATED.name(), "", operator);
    }

    /**
     * 回滚配置
     */
    @Transactional
    public void rollback(String currentVersionId, String targetVersionId, String operator) {
        // 根据版本号前缀确定配置类型
        String configType = getConfigTypeFromVersionId(currentVersionId);
        
        // 废弃当前版本
        updateConfigStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "", configType);
        // 启用目标版本（全量发布）
        updateConfigStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), "all", configType);
        
        // 记录发布历史
        recordHistory(targetVersionId, configType, ConfigStatus.PUBLISHED.name(), "all", operator);
    }

    /**
     * 回滚到上一个版本
     */
    @Transactional
    public void rollbackToPrevious(String identifier, String configType, String operator) {
        List<? extends ConfigIdentifier> publishedConfigs = getPublishedConfigs(identifier, configType);
        
        if (publishedConfigs.size() < 2) {
            throw new RuntimeException("No previous version to rollback for: " + identifier);
        }
        
        ConfigIdentifier currentConfig = publishedConfigs.get(0);
        ConfigIdentifier previousConfig = publishedConfigs.get(1);
        
        rollback(currentConfig.getVersionId(), previousConfig.getVersionId(), operator);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator) {
        List<? extends ConfigIdentifier> publishedConfigs = getPublishedConfigs(identifier, configType);
        
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

    private List<? extends ConfigIdentifier> getPublishedConfigs(String identifier, String configType) {
        String[] parts;
        switch (ConfigType.valueOf(configType)) {
            case DATA_SOURCE:
                return dataSourceConfigService.getPublishedByIdentifier(identifier);
            case API_RECORD:
                parts = identifier.split(":");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid API identifier format");
                }
                return apiRecordConfigService.getPublishedByIdentifier(
                    parts[0], parts[1], parts[2], parts[3]);
            case API_META:
                parts = identifier.split(":");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid API identifier format");
                }
                return apiMetaConfigService.getPublishedByIdentifier(
                    parts[0], parts[1], parts[2], parts[3]);
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    /**
     * 获取发布历史
     */
    public List<PublishHistory> getHistory(String versionId) {
        return publishHistoryMapper.findByVersionId(versionId);
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