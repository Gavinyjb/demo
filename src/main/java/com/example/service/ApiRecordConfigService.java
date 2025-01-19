package com.example.service;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.ApiRecordConfig;
import com.example.mapper.ApiRecordConfigMapper;
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
 * API记录配置服务
 */
@Service
public class ApiRecordConfigService implements BaseConfigService<ApiRecordConfig> {
    
    @Autowired
    private ApiRecordConfigMapper apiRecordConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional
    public ApiRecordConfig create(ApiRecordConfig config) {
        // 构建配置标识
        String identifier = buildIdentifier(config);
        
        // 检查是否已存在相同标识的配置
        List<ApiRecordConfig> existingConfigs = apiRecordConfigMapper.findPublishedConfigsByIdentifier(
            config.getGatewayType(),
            config.getGatewayCode(),
            config.getApiVersion(),
            config.getApiName()
        );
        if (!existingConfigs.isEmpty()) {
            throw new RuntimeException("Already exists config with identifier: " + identifier);
        }
        
        // 设置版本信息
        config.setVersionId(versionGenerator.generateApiRecordVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        
        // 插入配置和版本信息
        apiRecordConfigMapper.insertApiRecord(config);
        apiRecordConfigMapper.insertVersion(config);
        
        return config;
    }

    @Override
    @Transactional
    public ApiRecordConfig update(String oldVersionId, ApiRecordConfig newConfig) {
        ApiRecordConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        // 设置版本信息
        newConfig.setVersionId(versionGenerator.generateApiRecordVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        
        // 插入配置和版本信息
        apiRecordConfigMapper.insertApiRecord(newConfig);
        apiRecordConfigMapper.insertVersion(newConfig);
        
        return newConfig;
    }

    @Override
    public List<ApiRecordConfig> getAllPublished() {
        return apiRecordConfigMapper.findAllPublished();
    }

    @Override
    public ApiRecordConfig findByVersionId(String versionId) {
        return apiRecordConfigMapper.findByVersionId(versionId);
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String status, String stage) {
        // 更新版本状态
        apiRecordConfigMapper.updateVersionStatus(versionId, status);
        
        // 如果是发布状态且指定了灰度阶段，则插入灰度发布记录
        if (ConfigStatus.PUBLISHED.name().equals(status) && stage != null) {
            apiRecordConfigMapper.insertGrayRelease(versionId, stage);
        }
    }

    @Override
    public List<ApiRecordConfig> getActiveByRegion(String region) {
        String stage = regionProvider.getStageByRegion(region);
        return apiRecordConfigMapper.findByStage(stage);
    }

    @Override
    public List<ApiRecordConfig> getPublishedByIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return apiRecordConfigMapper.findPublishedConfigsByIdentifier(
            parts[0], parts[1], parts[2], parts[3]
        );
    }

    @Override
    public ApiRecordConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        String stage = regionProvider.getStageByRegion(region);
        return apiRecordConfigMapper.findActiveConfigByIdentifierAndStage(
            parts[0], parts[1], parts[2], parts[3], stage
        );
    }

    /**
     * 构建配置标识
     */
    private String buildIdentifier(ApiRecordConfig config) {
        return String.format("%s:%s:%s:%s",
            config.getGatewayType(),
            config.getGatewayCode(),
            config.getApiVersion(),
            config.getApiName()
        );
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse<ApiRecordConfig> getConfigDiff(ConfigDiffRequest request) {
        String stage = regionProvider.getStageByRegion(request.getRegion());
        
        // 获取当前所有生效的配置
        List<ApiRecordConfig> currentConfigs = apiRecordConfigMapper.findByStage(stage);
        
        // 找出新增或更新的配置
        Set<String> oldVersions = new HashSet<>(request.getVersionIds());
        List<ApiRecordConfig> updatedConfigs = currentConfigs.stream()
            .filter(config -> !oldVersions.contains(config.getVersionId()))
            .collect(Collectors.toList());
        
        // 找出已失效的版本
        List<String> deprecatedVersionIds = request.getVersionIds().stream()
            .filter(versionId -> currentConfigs.stream()
                .noneMatch(config -> config.getVersionId().equals(versionId)))
            .collect(Collectors.toList());
        
        // 构建响应
        return ConfigDiffResponse.<ApiRecordConfig>builder()
            .updatedConfigs(updatedConfigs)
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }
} 