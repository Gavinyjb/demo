package com.example.service;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.ApiRecordConfig;
import com.example.mapper.ApiRecordConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.model.bo.ApiRecordConfigBO;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.example.exception.ConfigNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * API记录配置服务
 */
@Service
@Slf4j
public class ApiRecordConfigService implements BaseConfigService<ApiRecordConfigBO> {
    
    @Autowired
    private ApiRecordConfigMapper apiRecordConfigMapper;

    @Autowired
    private VersionGenerator versionGenerator;

    @Autowired
    private RegionProvider regionProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiRecordConfigBO create(ApiRecordConfigBO configBO) {
        ApiRecordConfig config = configBO.toDO();
        try {
            // 检查是否已存在相同标识的配置
            List<ApiRecordConfig> existingConfigs = apiRecordConfigMapper.findPublishedByIdentifier(config.getIdentifier());
            if (!existingConfigs.isEmpty()) {
                throw new RuntimeException(String.format(
                    "Already exists config with identifier: %s:%s:%s:%s",
                    config.getGatewayType(),
                    config.getGatewayCode(),
                    config.getApiVersion(),
                    config.getApiName()
                ));
            }
            
            // 设置版本信息
            config.setVersionId(versionGenerator.generateApiRecordVersion());
            config.setConfigStatus(ConfigStatus.DRAFT.name());

            log.info("Inserting version record: {}", config);
            // 先插入版本信息
            apiRecordConfigMapper.insertVersion(config);
            
            log.info("Inserting api record: {}", config);
            // 再插入配置信息
            apiRecordConfigMapper.insertApiRecord(config);
            
            return ApiRecordConfigBO.fromDO(config);
        } catch (Exception e) {
            log.error("Failed to create api record config: {}", config, e);
            throw new RuntimeException("Failed to create api record config", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiRecordConfigBO update(String versionId, ApiRecordConfigBO configBO) {
        try {
            // 1. 验证原配置
            ApiRecordConfigBO oldConfig = findByVersionId(versionId);
            if (oldConfig == null) {
                throw new ConfigNotFoundException("Config not found: " + versionId);
            }

            // 2. 设置版本信息
            ApiRecordConfig config = configBO.toDO();
            config.setVersionId(versionGenerator.generateApiRecordVersion());
            config.setConfigStatus(ConfigStatus.DRAFT.name());

            log.info("Inserting version record for update: {}", config);
            // 3. 先插入版本信息
            apiRecordConfigMapper.insertVersion(config);

            log.info("Inserting api record for update: {}", config);
            // 4. 再插入配置信息
            apiRecordConfigMapper.insertApiRecord(config);

            return ApiRecordConfigBO.fromDO(config);
        } catch (Exception e) {
            log.error("Failed to update api record config: {}", configBO, e);
            throw new RuntimeException("Failed to update api record config", e);
        }
    }

    @Override
    public List<ApiRecordConfigBO> getAllPublished() {
        return apiRecordConfigMapper.findAllPublished().stream()
                .map(ApiRecordConfigBO::fromDO)
                .collect(Collectors.toList());
    }
    @Override
    public ApiRecordConfigBO findByVersionId(String versionId) {
        return ApiRecordConfigBO.fromDO(apiRecordConfigMapper.findByVersionId(versionId));
    }

    @Override
    public List<ApiRecordConfigBO> getActiveByRegion(String region) {
        String stage = regionProvider.getStageByRegion(region);
        return apiRecordConfigMapper.findByStage(stage).stream()
            .map(ApiRecordConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ApiRecordConfigBO> getPublishedByIdentifier(String identifier) {
        return apiRecordConfigMapper.findPublishedByIdentifier(identifier).stream()
            .map(ApiRecordConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiRecordConfigBO getActiveByIdentifierAndRegion(String identifier, String region) {
        String stage = regionProvider.getStageByRegion(region);
        ApiRecordConfig config = apiRecordConfigMapper.findActiveConfigByIdentifierAndStage(identifier, stage);
        return config != null ? ApiRecordConfigBO.fromDO(config) : null;
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse<ApiRecordConfigBO> getConfigDiff(ConfigDiffRequest request) {
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
        return ConfigDiffResponse.<ApiRecordConfigBO>builder()
            .updatedConfigs(updatedConfigs.stream()
                .map(ApiRecordConfigBO::fromDO)
                .collect(Collectors.toList()))
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }

    @Override
    public void deleteByVersionId(String versionId) {
        apiRecordConfigMapper.deleteByVersionId(versionId);
    }

    @Override
    public int getMaxDeprecatedVersions() {
        return 8;
    }
} 