package com.example.service;

import com.example.enums.ConfigStatus;
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

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_DELAY_MS = 1000;

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

        publishByStage(targetVersionId, targetHistory.getConfigType(), GrayStage.FULL, operator);
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
    @Transactional(rollbackFor = Exception.class)
    public void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator) {
        // 1. 查找目标版本
        ConfigVersion targetVersion = configVersionMapper.findByVersionId(targetVersionId);
        if (targetVersion == null || !ConfigStatus.DEPRECATED.name().equals(targetVersion.getConfigStatus())) {
            throw new RuntimeException("Invalid target version");
        }

        // 2. 废弃当前生效的版本
        ConfigVersion currentVersion = configVersionMapper.findActiveVersionByIdentifier(identifier, configType);
        if (currentVersion != null) {
            configVersionMapper.updateStatus(currentVersion.getVersionId(), ConfigStatus.DEPRECATED.name());
            grayReleaseMapper.deleteByVersionId(currentVersion.getVersionId());
        }

        // 3. 激活目标版本
        configVersionMapper.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name());
        grayReleaseMapper.insert(targetVersionId, GrayStage.FULL.name());

        // 4. 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(targetVersionId)
            .configType(configType)
            .configStatus(ConfigStatus.PUBLISHED.name())
            .stage(GrayStage.FULL.name())
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
        
        log.info("Successfully rolled back to version: {}", targetVersionId);
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
        int retryCount = 0;
        while (true) {
            try {
                doPublishByStage(versionId, configType, stage, operator);
                break;
            } catch (Exception e) {
                if (isDuplicateKeyError(e) && retryCount < MAX_RETRY_TIMES) {
                    retryCount++;
                    log.warn("Duplicate key error when publishing, will retry after {} ms. Retry count: {}", 
                        RETRY_DELAY_MS, retryCount);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Publishing interrupted", ie);
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void doPublishByStage(String versionId, String configType, GrayStage stage, String operator) {
        // 1. 验证配置版本
        ConfigVersion version = configVersionMapper.findByVersionId(versionId);
        if (version == null) {
            throw new RuntimeException("Config version not found: " + versionId);
        }

        // 2. 检查是否存在灰度中的配置
        if (stage != GrayStage.FULL) {
            ConfigVersion grayingVersion = configVersionMapper.findVersionByIdentifierAndStatus(
                version.getIdentifier(),
                configType,
                ConfigStatus.GRAYING.name()
            );
            if (grayingVersion != null && !grayingVersion.getVersionId().equals(versionId)) {
                throw new IllegalStateException(
                    String.format("Config %s already has a graying version: %s", 
                        version.getIdentifier(), 
                        grayingVersion.getVersionId()
                    )
                );
            }

            // 检查当前阶段
            String currentStage = grayReleaseMapper.findStageByVersionId(versionId);
            if (currentStage != null && getStageOrder(stage) < getStageOrder(GrayStage.valueOf(currentStage))) {
                throw new RuntimeException("Cannot publish to lower stage: " + stage);
            }
        }

        // 3. 更新配置状态
        String status = stage == GrayStage.FULL ? 
            ConfigStatus.PUBLISHED.name() : 
            ConfigStatus.GRAYING.name();
        configVersionMapper.updateStatus(versionId, status);

        // 4. 记录灰度发布信息
        grayReleaseMapper.deleteByVersionId(versionId);
        grayReleaseMapper.insert(versionId, stage.name());

        // 5. 如果是全量发布，需要废弃旧的全量发布版本
        if (stage == GrayStage.FULL) {
            deprecateOldVersions(version.getIdentifier(), configType, versionId);
        }

        // 6. 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(versionId)
            .configType(configType)
            .configStatus(status)
            .stage(stage.name())
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
        
        log.info("Successfully published config {} to stage {} with status {}", versionId, stage, status);
    }

    private int getStageOrder(GrayStage stage) {
        switch (stage) {
            case STAGE_1:
                return 1;
            case STAGE_2:
                return 2;
            case FULL:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * 废弃旧的全量发布版本
     */
    private void deprecateOldVersions(String identifier, String configType, String currentVersionId) {
        // 查找同一标识的其他已发布且为全量发布的版本
        List<ConfigVersion> publishedVersions = configVersionMapper.findPublishedFullVersionsByIdentifier(
            identifier, 
            configType,
            currentVersionId
        );
        
        // 检查全量发布版本数量
        if (publishedVersions.size() != 1) {
            log.error("Unexpected number of published full versions for {}: expected 1, got {}. Versions: {}", 
                identifier, 
                publishedVersions.size(),
                publishedVersions.stream()
                    .map(ConfigVersion::getVersionId)
                    .collect(Collectors.joining(", "))
            );
        }
        
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

    /**
     * 终止灰度发布
     */
    @Transactional(rollbackFor = Exception.class)
    public void rollbackGrayConfig(String identifier, String configType, String operator) {
        // 1. 查找当前灰度中的配置
        ConfigVersion grayingVersion = configVersionMapper.findVersionByIdentifierAndStatus(
            identifier, configType, ConfigStatus.GRAYING.name()
        );

        if (grayingVersion == null) {
            throw new RuntimeException("No graying config found");
        }

        // 2. 废弃灰度配置
        configVersionMapper.updateStatus(grayingVersion.getVersionId(), ConfigStatus.DEPRECATED.name());
        grayReleaseMapper.deleteByVersionId(grayingVersion.getVersionId());

        // 3. 记录发布历史
        PublishHistory history = PublishHistory.builder()
            .versionId(grayingVersion.getVersionId())
            .configType(configType)
            .configStatus(ConfigStatus.DEPRECATED.name())
            .stage(null)
            .operator(operator)
            .build();
        publishHistoryMapper.insert(history);
        
        log.info("Successfully rolled back graying config: {}", grayingVersion.getVersionId());
    }

    private boolean isDuplicateKeyError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                String message = cause.getMessage();
                return message != null && message.contains("Duplicate entry");
            }
            cause = cause.getCause();
        }
        return false;
    }
} 