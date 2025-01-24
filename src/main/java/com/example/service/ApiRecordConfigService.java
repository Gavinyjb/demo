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
import java.util.Arrays;
import com.example.enums.GrayStage;

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
     * 获取配置差异信息
     * 比较客户端版本与服务端版本的差异，返回需要更新的配置
     */
    public ConfigDiffResponse<ApiRecordConfigBO> getConfigDiff(List<String> clientVersionIds, String region) {
        // 获取该地域当前生效的所有配置
        List<ApiRecordConfigBO> activeConfigs = getActiveByRegion(region);
        
        // 当前生效的版本号列表
        List<String> activeVersionIds = activeConfigs.stream()
            .map(ApiRecordConfigBO::getVersionId)
            .collect(Collectors.toList());
            
        // 客户端需要更新的配置（新增或更新的）
        List<ApiRecordConfigBO> updatedConfigs = activeConfigs.stream()
            .filter(config -> !clientVersionIds.contains(config.getVersionId()))
            .collect(Collectors.toList());
            
        // 已失效的版本号（客户端持有但服务端已不存在的）
        List<String> deprecatedVersionIds = clientVersionIds.stream()
            .filter(versionId -> !activeVersionIds.contains(versionId))
            .collect(Collectors.toList());
            
        return ConfigDiffResponse.<ApiRecordConfigBO>builder()
            .updatedConfigs(updatedConfigs)
            .activeVersionIds(activeVersionIds)
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

    /**
     * 删除指定版本ID的配置（仅删除草稿或废弃状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByVersionIdWithStatusCheck(String versionId) {
        // 先查询配置状态
        ApiRecordConfigBO config = findByVersionId(versionId);
        if (config == null) {
            throw new ConfigNotFoundException("Config not found: " + versionId);
        }
        
        // 检查配置状态
        if (ConfigStatus.PUBLISHED.name().equals(config.getConfigStatus()) || 
            ConfigStatus.GRAYING.name().equals(config.getConfigStatus())) {
            throw new RuntimeException("Cannot delete published or graying config: " + versionId);
        }

        apiRecordConfigMapper.deleteByVersionIdAndStatusIn(versionId, 
            Arrays.asList(ConfigStatus.DRAFT.name(), ConfigStatus.DEPRECATED.name()));
    }

    /**
     * 删除指定标识的配置（仅删除草稿或废弃状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIdentifierWithStatusCheck(String identifier) {
        // 不需要额外检查，因为SQL中已经限制了只删除草稿和废弃状态的配置
        apiRecordConfigMapper.deleteByIdentifierAndStatusIn(identifier,
            Arrays.asList(ConfigStatus.DRAFT.name(), ConfigStatus.DEPRECATED.name()));
    }

    /**
     * 按状态查询配置
     */
    public List<ApiRecordConfigBO> findByStatus(List<String> statusList) {
        List<ApiRecordConfig> configs = apiRecordConfigMapper.findByStatus(statusList);
        return configs.stream()
            .map(ApiRecordConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    /**
     * 按标识和状态查询配置
     */
    public List<ApiRecordConfigBO> findByIdentifierAndStatus(String identifier, List<String> statusList) {
        List<ApiRecordConfig> configs = apiRecordConfigMapper.findByIdentifierAndStatus(identifier, statusList);
        return configs.stream()
            .map(ApiRecordConfigBO::fromDO)
            .collect(Collectors.toList());
    }

//    /**
//     * 根据标识符和灰度阶段查找生效的配置
//     */
//    public ApiRecordConfigBO findActiveConfigByIdentifierAndStage(String identifier, String stage) {
//        // 1. 查找所有处于灰度和发布状态的配置
//        List<ApiRecordConfigBO> activeConfigs = apiRecordConfigMapper.findByIdentifierAndStatus(
//            identifier,
//            Arrays.asList(ConfigStatus.GRAYING.name(), ConfigStatus.PUBLISHED.name())
//        );
//
//        // 2. 校验配置数量
//        if (activeConfigs.size() > 2) {
//            throw new IllegalStateException(
//                String.format("Found more than 2 active configs for identifier: %s", identifier)
//            );
//        }
//
//        // 3. 如果只有一个配置且是发布状态，直接返回
//        if (activeConfigs.size() == 1 &&
//            ConfigStatus.PUBLISHED.name().equals(activeConfigs.get(0).getConfigStatus())) {
//            return activeConfigs.get(0);
//        }
//
//        // 4. 如果有灰度配置和发布配置，根据stage判断返回哪个配置
//        if (activeConfigs.size() == 2) {
//            ApiRecordConfigBO grayConfig = activeConfigs.stream()
//                .filter(c -> ConfigStatus.GRAY.name().equals(c.getConfigStatus()))
//                .findFirst()
//                .get();
//            ApiRecordConfigBO publishedConfig = activeConfigs.stream()
//                .filter(c -> ConfigStatus.PUBLISHED.name().equals(c.getConfigStatus()))
//                .findFirst()
//                .get();
//
//            // 阶段一直接返回灰度配置
//            if (GrayStage.STAGE_1.name().equals(stage)) {
//                return grayConfig;
//            }
//
//            // 阶段二判断灰度配置是否是阶段二或阶段三
//            if (GrayStage.STAGE_2.name().equals(stage)) {
//                String grayStage = grayConfig.getStage();
//                if (GrayStage.STAGE_2.name().equals(grayStage) ||
//                    GrayStage.STAGE_3.name().equals(grayStage)) {
//                    return grayConfig;
//                }
//                return publishedConfig;
//            }
//
//            // 阶段三判断灰度配置是否是阶段三
//            if (GrayStage.STAGE_3.name().equals(stage)) {
//                if (GrayStage.STAGE_3.name().equals(grayConfig.getStage())) {
//                    return grayConfig;
//                }
//                return publishedConfig;
//            }
//
//            // 其他情况返回发布配置
//            return publishedConfig;
//        }
//
//        // 5. 其他情况返回null
//        return null;
//    }
} 