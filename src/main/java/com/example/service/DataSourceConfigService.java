package com.example.service;

import com.example.model.DataSourceConfig;
import com.example.model.bo.DataSourceConfigBO;
import com.example.mapper.DataSourceConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import com.example.config.VersionProperties;
import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 数据源配置服务
 */
@Slf4j
@Service
public class DataSourceConfigService implements BaseConfigService<DataSourceConfigBO> {
    
    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSourceConfigBO create(DataSourceConfigBO configBO) {
        DataSourceConfig config = configBO.toDO();
        try {
            // 检查是否已存在相同标识的配置
            List<DataSourceConfig> existingConfigs = dataSourceConfigMapper.findPublishedConfigsByName(config.getName());
            if (!existingConfigs.isEmpty()) {
                throw new RuntimeException("Already exists config with name: " + config.getName());
            }
            
            // 设置版本信息
            config.setVersionId(versionGenerator.generateDataSourceVersion());
            config.setConfigStatus(ConfigStatus.DRAFT.name());
            config.setStatus(0); // 数据源状态：未上线

            log.info("Inserting version record: {}", config);
            // 先插入版本信息
            try {
                dataSourceConfigMapper.insertVersion(config);
            } catch (Exception e) {
                log.error("Failed to insert version record", e);
                throw e;
            }
            
            log.info("Inserting data source record: {}", config);
            // 再插入配置信息
            try {
                dataSourceConfigMapper.insertDataSource(config);
            } catch (Exception e) {
                log.error("Failed to insert data source record", e);
                throw e;
            }
            
            return DataSourceConfigBO.fromDO(config);
        } catch (Exception e) {
            log.error("Failed to create data source config: {}", config, e);
            throw new RuntimeException("Failed to create data source config", e);
        }
    }

    @Override
    @Transactional
    public DataSourceConfigBO update(String oldVersionId, DataSourceConfigBO newConfigBO) {
        DataSourceConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        // 设置版本信息
        newConfigBO.setVersionId(versionGenerator.generateDataSourceVersion());
        // 保持原有数据源状态
        newConfigBO.setStatus(oldConfig.getStatus());
        
        try {
            // 先插入版本信息
            dataSourceConfigMapper.insertVersion(newConfigBO.toDO());
            
            // 再插入配置信息
            dataSourceConfigMapper.insertDataSource(newConfigBO.toDO());
            
            // 清理旧的草稿版本
            cleanupOldVersions(newConfigBO.getName());
            
            return newConfigBO;
        } catch (Exception e) {
            log.error("Failed to update data source config", e);
            throw new RuntimeException("Failed to update data source config", e);
        }
    }

    @Override
    public List<DataSourceConfigBO> getAllPublished() {
        return dataSourceConfigMapper.findAllPublished().stream()
            .map(DataSourceConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public DataSourceConfigBO findByVersionId(String versionId) {
        DataSourceConfig config = dataSourceConfigMapper.findByVersionId(versionId);
        return config != null ? DataSourceConfigBO.fromDO(config) : null;
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String configStatus, String stage) {
        // 更新版本状态
        dataSourceConfigMapper.updateVersionStatus(versionId, configStatus);
        
        // 如果是发布状态且指定了灰度阶段，则插入灰度发布记录
        if (ConfigStatus.PUBLISHED.name().equals(configStatus) && stage != null) {
            dataSourceConfigMapper.insertGrayRelease(versionId, stage);
        }
    }

    @Override
    public List<DataSourceConfigBO> getActiveByRegion(String region) {
        String stage = regionProvider.getStageByRegion(region);
        return dataSourceConfigMapper.findByStage(stage).stream()
            .map(DataSourceConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public List<DataSourceConfigBO> getPublishedByIdentifier(String identifier) {
        return dataSourceConfigMapper.findPublishedConfigsByName(identifier).stream()
            .map(DataSourceConfigBO::fromDO)
            .collect(Collectors.toList());
    }

    @Override
    public DataSourceConfigBO getActiveByIdentifierAndRegion(String identifier, String region) {
        String stage = regionProvider.getStageByRegion(region);
        DataSourceConfig config = dataSourceConfigMapper.findActiveConfigByNameAndStage(identifier, stage);
        return config != null ? DataSourceConfigBO.fromDO(config) : null;
    }

    /**
     * 清理旧版本
     */
    private void cleanupOldVersions(String name) {
        dataSourceConfigMapper.deleteByNameAndStatus(name, ConfigStatus.DRAFT.name());
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse<DataSourceConfigBO> getConfigDiff(ConfigDiffRequest request) {
        String stage = regionProvider.getStageByRegion(request.getRegion());
        
        // 获取当前所有生效的配置
        List<DataSourceConfig> currentConfigs = dataSourceConfigMapper.findByStage(stage);
        
        // 找出新增或更新的配置
        Set<String> oldVersions = new HashSet<>(request.getVersionIds());
        List<DataSourceConfig> updatedConfigs = currentConfigs.stream()
            .filter(config -> !oldVersions.contains(config.getVersionId()))
            .collect(Collectors.toList());
        
        // 找出已失效的版本
        List<String> deprecatedVersionIds = request.getVersionIds().stream()
            .filter(versionId -> currentConfigs.stream()
                .noneMatch(config -> config.getVersionId().equals(versionId)))
            .collect(Collectors.toList());
        
        // 构建响应
        return ConfigDiffResponse.<DataSourceConfigBO>builder()
            .updatedConfigs(updatedConfigs.stream()
                .map(DataSourceConfigBO::fromDO)
                .collect(Collectors.toList()))
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }
} 