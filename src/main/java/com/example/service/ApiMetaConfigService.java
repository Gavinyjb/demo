package com.example.service;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.ApiMetaConfig;
import com.example.mapper.ApiMetaConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.model.bo.ApiMetaConfigBO;
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
import lombok.extern.slf4j.Slf4j;

/**
 * API Meta配置服务
 */
@Slf4j
@Service
public class ApiMetaConfigService implements BaseConfigService<ApiMetaConfigBO> {
    
    @Autowired
    private ApiMetaConfigMapper apiMetaConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiMetaConfigBO create(ApiMetaConfigBO configBO) {
        ApiMetaConfig config = configBO.toDO();
        try {
            // 检查是否已存在相同标识的配置
            List<ApiMetaConfig> existingConfigs = apiMetaConfigMapper.findPublishedByIdentifier(config.getIdentifier());
            if (!existingConfigs.isEmpty()) {
                throw new RuntimeException("Already exists config with identifier: " + config.getIdentifier());
            }
            
            // 设置版本信息
            config.setVersionId(versionGenerator.generateApiMetaVersion());
            config.setConfigStatus(ConfigStatus.DRAFT.name());

            log.info("Inserting version record: {}", config);
            // 先插入版本信息
            apiMetaConfigMapper.insertVersion(config);
            log.info("Inserting api meta record: {}", config);
            // 再插入配置信息
            apiMetaConfigMapper.insertApiMeta(config);            
            return ApiMetaConfigBO.fromDO(config);
        } catch (Exception e) {
            log.error("Failed to create api meta config: {}", config, e);
            throw new RuntimeException("Failed to create api meta config", e);
        }
    }

    @Override
    @Transactional
    public ApiMetaConfigBO update(String oldVersionId, ApiMetaConfigBO newConfigBO) {
        ApiMetaConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        // 设置版本信息
        newConfigBO.setVersionId(versionGenerator.generateApiMetaVersion());
        newConfigBO.setConfigStatus(ConfigStatus.DRAFT.name());
        
        try {
            // 先插入版本信息
            apiMetaConfigMapper.insertVersion(newConfigBO.toDO());
            
            // 再插入配置信息
            apiMetaConfigMapper.insertApiMeta(newConfigBO.toDO());
            
            return newConfigBO;
        } catch (Exception e) {
            log.error("Failed to update api meta config", e);
            throw new RuntimeException("Failed to update api meta config", e);
        }
    }

    @Override
    public List<ApiMetaConfigBO> getAllPublished() {
        return apiMetaConfigMapper.findAllPublished().stream()
            .map(ApiMetaConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiMetaConfigBO findByVersionId(String versionId) {
        ApiMetaConfig config = apiMetaConfigMapper.findByVersionId(versionId);
        return config != null ? ApiMetaConfigBO.fromDO(config) : null;
    }

    @Override
    public List<ApiMetaConfigBO> getActiveByRegion(String region) {
        String stage = regionProvider.getStageByRegion(region);
        return apiMetaConfigMapper.findByStage(stage).stream()
            .map(ApiMetaConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ApiMetaConfigBO> getPublishedByIdentifier(String identifier) {
        return apiMetaConfigMapper.findPublishedByIdentifier(identifier).stream()
            .map(ApiMetaConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiMetaConfigBO getActiveByIdentifierAndRegion(String identifier, String region) {
        String stage = regionProvider.getStageByRegion(region);
        ApiMetaConfig config = apiMetaConfigMapper.findActiveConfigByIdentifierAndStage(identifier, stage);
        return config != null ? ApiMetaConfigBO.fromDO(config) : null;
    }

    public ConfigDiffResponse<ApiMetaConfigBO> getConfigDiff(ConfigDiffRequest request) {
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
        return ConfigDiffResponse.<ApiMetaConfigBO>builder()
            .updatedConfigs(updatedConfigs.stream()
                .map(ApiMetaConfigBO::fromDO)
                .collect(Collectors.toList()))
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }

    @Override
    public void deleteByVersionId(String versionId) {
        apiMetaConfigMapper.deleteByVersionId(versionId);
    }

    @Override
    public int getMaxDeprecatedVersions() {
        return 5;
    }
} 