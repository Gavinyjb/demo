package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.ApiRecordConfig;
import com.example.model.DataSourceConfig;
import com.example.model.PublishHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;

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
        // 废弃配置时不需要指定灰度组
        dataSourceConfigService.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), "");
        apiRecordConfigService.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), "");
        apiMetaConfigService.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), "");
        
        recordHistory(versionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.DEPRECATED.name(), "", operator);
        recordHistory(versionId, ConfigType.API_RECORD.name(), ConfigStatus.DEPRECATED.name(), "", operator);
        recordHistory(versionId, ConfigType.API_META.name(), ConfigStatus.DEPRECATED.name(), "", operator);
    }

    /**
     * 回滚到上一个版本
     */
    @Transactional
    public void rollbackToPrevious(String identifier, String configType, String operator) {
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            List<DataSourceConfig> publishedConfigs = 
                dataSourceConfigService.getPublishedByIdentifier(identifier);
            
            if (publishedConfigs.size() < 2) {
                throw new RuntimeException("No previous version to rollback for source: " + identifier);
            }
            
            DataSourceConfig currentConfig = publishedConfigs.get(0);
            DataSourceConfig previousConfig = publishedConfigs.get(1);
            
            rollback(currentConfig.getVersionId(), previousConfig.getVersionId(), operator);
        } else if (ConfigType.API_RECORD.name().equals(configType)) {
            String[] parts = identifier.split(":");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid API identifier format");
            }
            
            List<ApiRecordConfig> publishedConfigs = 
                apiRecordConfigService.getPublishedByIdentifier(parts[0], parts[1], parts[2], parts[3]);
            
            if (publishedConfigs.size() < 2) {
                throw new RuntimeException("No previous version to rollback for API: " + identifier);
            }
            
            ApiRecordConfig currentConfig = publishedConfigs.get(0);
            ApiRecordConfig previousConfig = publishedConfigs.get(1);
            
            rollback(currentConfig.getVersionId(), previousConfig.getVersionId(), operator);
        }
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator) {
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            List<DataSourceConfig> publishedConfigs = 
                dataSourceConfigService.getPublishedByIdentifier(identifier);
            if (publishedConfigs.isEmpty()) {
                throw new RuntimeException("No active version for source: " + identifier);
            }
            
            rollback(publishedConfigs.get(0).getVersionId(), targetVersionId, operator);
        } else if (ConfigType.API_RECORD.name().equals(configType)) {
            String[] parts = identifier.split(":");
            List<ApiRecordConfig> publishedConfigs = 
                apiRecordConfigService.getPublishedByIdentifier(parts[0], parts[1], parts[2], parts[3]);
            if (publishedConfigs.isEmpty()) {
                throw new RuntimeException("No active version for API: " + identifier);
            }
            
            rollback(publishedConfigs.get(0).getVersionId(), targetVersionId, operator);
        }
    }

    /**
     * 回滚配置
     */
    @Transactional
    public void rollback(String currentVersionId, String targetVersionId, String operator) {
        // 回滚时，目标版本使用全量发布
        dataSourceConfigService.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
        dataSourceConfigService.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), "all");
        apiRecordConfigService.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
        apiRecordConfigService.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), "all");
        
        apiMetaConfigService.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
        apiMetaConfigService.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), "all");
        
        recordHistory(targetVersionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.PUBLISHED.name(), "all", operator);
        recordHistory(targetVersionId, ConfigType.API_RECORD.name(), ConfigStatus.PUBLISHED.name(), "all", operator);
        recordHistory(targetVersionId, ConfigType.API_META.name(), ConfigStatus.PUBLISHED.name(), "all", operator);
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