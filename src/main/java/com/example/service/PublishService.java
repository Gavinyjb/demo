package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.ConfigVersionMapper;
import com.example.mapper.ConfigGrayReleaseMapper;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.BaseVersionedConfig;
import com.example.model.ConfigVersion;
import com.example.model.PublishHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 配置发布服务
 */
@Slf4j
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

    @Autowired
    private ConfigVersionMapper configVersionMapper;
    
    @Autowired
    private ConfigGrayReleaseMapper grayReleaseMapper;

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

    /**
     * 按阶段发布配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishByStage(String versionId, String configType, GrayStage stage, String operator) {
        log.info("Publishing config {} to stage {}", versionId, stage);
        
        // 1. 验证配置版本
        ConfigVersion version = configVersionMapper.findByVersionId(versionId);
        if (version == null) {
            throw new RuntimeException("Config version not found: " + versionId);
        }

        // 2. 检查当前版本状态
        if (ConfigStatus.DEPRECATED.name().equals(version.getConfigStatus())) {
            throw new RuntimeException("Cannot publish deprecated version: " + versionId);
        }

        // 3. 更新配置状态为已发布
        configVersionMapper.updateStatus(versionId, ConfigStatus.PUBLISHED.name());
        
        // 4. 记录灰度发布信息
        String currentStage = grayReleaseMapper.findStageByVersionId(versionId);
        if (currentStage != null) {
            if (GrayStage.valueOf(currentStage).ordinal() >= stage.ordinal()) {
                throw new RuntimeException("Cannot publish to lower stage: " + stage);
            }
            grayReleaseMapper.deleteByVersionId(versionId);
        }
        grayReleaseMapper.insert(versionId, stage.name());
        
        // 5. 如果是全量发布，需要废弃旧版本
        if (stage == GrayStage.FULL) {
            deprecateOldVersions(version.getIdentifier(), configType, versionId);
        }
        
        // 6. 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(versionId)
            .configType(configType)
            .configStatus(ConfigStatus.PUBLISHED.name())
            .stage(stage.name())
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
        
        log.info("Successfully published config {} to stage {}", versionId, stage);
    }

    /**
     * 废弃旧版本
     */
    private void deprecateOldVersions(String identifier, String configType, String currentVersionId) {
        // 查找同一标识的其他已发布版本
        List<ConfigVersion> publishedVersions = configVersionMapper.findPublishedVersionsByIdentifier(
            identifier, 
            configType,
            currentVersionId
        );
        
        // 将这些版本标记为废弃
        for (ConfigVersion oldVersion : publishedVersions) {
            configVersionMapper.updateStatus(oldVersion.getVersionId(), ConfigStatus.DEPRECATED.name());
            grayReleaseMapper.deleteByVersionId(oldVersion.getVersionId());
            log.info("Deprecated old version: {}", oldVersion.getVersionId());
        }

        // 清理过期的废弃版本
        cleanupDeprecatedVersions(identifier, configType);
    }

    /**
     * 清理过期的废弃版本
     */
    private void cleanupDeprecatedVersions(String identifier, String configType) {
        BaseConfigService<?> service = getConfigService(configType);
        int maxDeprecatedVersions = service.getMaxDeprecatedVersions();
        
        List<ConfigVersion> deprecatedVersions = configVersionMapper.findDeprecatedVersionsByIdentifier(
            identifier, 
            configType
        );

        if (deprecatedVersions.size() > maxDeprecatedVersions) {
            deprecatedVersions.stream()
                .sorted((v1, v2) -> v1.getGmtCreate().compareTo(v2.getGmtCreate()))
                .limit(deprecatedVersions.size() - maxDeprecatedVersions)
                .forEach(version -> {
                    deleteConfigVersion(version.getVersionId(), configType);
                    log.info("Deleted old deprecated version: {}", version.getVersionId());
                });
        }
    }

    /**
     * 删除配置版本及其关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    private void deleteConfigVersion(String versionId, String configType) {
        // 1. 删除灰度发布记录
        grayReleaseMapper.deleteByVersionId(versionId);
        
        // 2. 删除发布历史
        publishHistoryMapper.deleteByVersionId(versionId);
        
        // 3. 删除配置元数据
        switch (configType) {
            case "DATA_SOURCE":
                dataSourceConfigService.deleteByVersionId(versionId);
                break;
            case "API_RECORD":
                apiRecordConfigService.deleteByVersionId(versionId);
                break;
            case "API_META":
                apiMetaConfigService.deleteByVersionId(versionId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
        
        // 4. 删除版本记录
        configVersionMapper.deleteByVersionId(versionId);
    }

    /**
     * 废弃配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void deprecateConfig(String versionId, String configType, String operator) {
        // 1. 验证配置版本
        ConfigVersion version = configVersionMapper.findByVersionId(versionId);
        if (version == null) {
            throw new RuntimeException("Config version not found: " + versionId);
        }

        // 2. 更新状态为废弃
        configVersionMapper.updateStatus(versionId, ConfigStatus.DEPRECATED.name());
        
        // 3. 删除灰度发布记录
        grayReleaseMapper.deleteByVersionId(versionId);
        
        // 4. 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(versionId)
            .configType(configType)
            .configStatus(ConfigStatus.DEPRECATED.name())
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
        
        log.info("Successfully deprecated config: {}", versionId);
    }

    /**
     * 获取指定配置类型和灰度阶段的发布历史
     */
    public List<PublishHistory> getHistoryByTypeAndStage(String configType, String stage) {
        return publishHistoryMapper.findByConfigTypeAndStage(configType, stage);
    }
} 