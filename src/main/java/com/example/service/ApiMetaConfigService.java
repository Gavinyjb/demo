package com.example.service;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.ApiMetaConfig;
import com.example.mapper.ApiMetaConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import com.example.config.VersionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * API Meta配置服务
 */
@Service
public class ApiMetaConfigService implements BaseConfigService<ApiMetaConfig> {
    
    @Autowired
    private ApiMetaConfigMapper apiMetaConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional
    public ApiMetaConfig create(ApiMetaConfig config) {
        if (hasSameApiMetaConfig(config)) {
            throw new RuntimeException("Already exists config with identifier: " + config.getIdentifier());
        }
        
        config.setVersionId(versionGenerator.generateApiMetaVersion());
        config.setConfigStatus(ConfigStatus.DRAFT.name());
        apiMetaConfigMapper.insertApiMeta(config);
        apiMetaConfigMapper.insertVersion(config);
        
        cleanupOldVersions(config);
        
        return config;
    }

    @Override
    @Transactional
    public ApiMetaConfig update(String oldVersionId, ApiMetaConfig newConfig) {
        ApiMetaConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateApiMetaVersion());
        newConfig.setConfigStatus(ConfigStatus.DRAFT.name());
        apiMetaConfigMapper.insertApiMeta(newConfig);
        apiMetaConfigMapper.insertVersion(newConfig);
        
        cleanupOldVersions(newConfig);
        
        return newConfig;
    }

    @Override
    public List<ApiMetaConfig> getAllPublished() {
        return apiMetaConfigMapper.findAllPublished();
    }

    @Override
    public ApiMetaConfig findByVersionId(String versionId) {
        return apiMetaConfigMapper.findByVersionId(versionId);
    }


    @Override
    public List<ApiMetaConfig> getActiveByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        String stage = regionProvider.getStageByRegion(region);
        return apiMetaConfigMapper.findByStage(stage);
    }

    @Override
    public List<ApiMetaConfig> getPublishedByIdentifier(String identifier) {
        return apiMetaConfigMapper.findPublishedConfigsByIdentifier(identifier);
    }

    @Override
    public ApiMetaConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        String stage = regionProvider.getStageByRegion(region);
        return apiMetaConfigMapper.findActiveConfigByIdentifierAndStage(identifier, stage);
    }

    @Transactional
    public void publish(String versionId, String stage, String operator) {
        apiMetaConfigMapper.updateVersionStatus(versionId, ConfigStatus.PUBLISHED.name());
        apiMetaConfigMapper.insertGrayRelease(versionId, stage);
    }

    private boolean hasSameApiMetaConfig(ApiMetaConfig config) {
        List<ApiMetaConfig> existingConfigs = getAllPublished();
        return existingConfigs.stream()
            .anyMatch(existing -> existing.getIdentifier().equals(config.getIdentifier()));
    }

    private void cleanupOldVersions(ApiMetaConfig config) {
        List<ApiMetaConfig> allVersions = apiMetaConfigMapper.findPublishedConfigsByIdentifier(config.getIdentifier());
        if (allVersions.size() > versionProperties.getMaxApiMetaVersions()) {
            allVersions.stream()
                .skip(versionProperties.getMaxApiMetaVersions())
                .forEach(c -> apiMetaConfigMapper.deleteByVersionId(c.getVersionId()));
        }
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse<ApiMetaConfig> getConfigDiff(ConfigDiffRequest request) {
        String stage = regionProvider.getStageByRegion(request.getRegion());
        
        // 获取当前所有生效的配置
        List<ApiMetaConfig> currentConfigs = apiMetaConfigMapper.findByStage(stage);
        
        // 找出新增或更新的配置
        Set<String> oldVersions = new HashSet<>(request.getVersionIds());
        List<ApiMetaConfig> updatedConfigs = currentConfigs.stream()
            .filter(config -> !oldVersions.contains(config.getVersionId()))
            .collect(Collectors.toList());
        
        // 找出已失效的版本
        List<String> deprecatedVersionIds = request.getVersionIds().stream()
            .filter(versionId -> currentConfigs.stream()
                .noneMatch(config -> config.getVersionId().equals(versionId)))
            .collect(Collectors.toList());
        
        // 构建响应
        return ConfigDiffResponse.<ApiMetaConfig>builder()
            .updatedConfigs(updatedConfigs)
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }

    @Override
    public void deleteByVersionId(String versionId) {
        apiMetaConfigMapper.deleteByVersionId(versionId);
    }

    @Override
    public int getMaxDeprecatedVersions() {
        return 8;
    }
} 