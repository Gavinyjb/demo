package com.example.service;

import com.example.model.DataSourceConfig;
import com.example.mapper.DataSourceConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import com.example.config.VersionProperties;
import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
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
@Service
public class DataSourceConfigService implements BaseConfigService<DataSourceConfig> {
    
    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional
    public DataSourceConfig create(DataSourceConfig config) {
        // 检查是否已存在相同标识的配置
        List<DataSourceConfig> existingConfigs = dataSourceConfigMapper.findPublishedConfigsBySource(config.getSource());
        if (!existingConfigs.isEmpty()) {
            throw new RuntimeException("Already exists config with source: " + config.getSource());
        }
        
        // 设置版本信息
        config.setVersionId(versionGenerator.generateDataSourceVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        
        // 插入配置和版本信息
        dataSourceConfigMapper.insertDataSource(config);
        dataSourceConfigMapper.insertVersion(config);
        
        // 清理旧的草稿版本
        cleanupOldVersions(config.getSource());
        
        return config;
    }

    @Override
    @Transactional
    public DataSourceConfig update(String oldVersionId, DataSourceConfig newConfig) {
        DataSourceConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        // 设置版本信息
        newConfig.setVersionId(versionGenerator.generateDataSourceVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        
        // 插入配置和版本信息
        dataSourceConfigMapper.insertDataSource(newConfig);
        dataSourceConfigMapper.insertVersion(newConfig);
        
        // 清理旧的草稿版本
        cleanupOldVersions(newConfig.getSource());
        
        return newConfig;
    }

    @Override
    public List<DataSourceConfig> getAllPublished() {
        return dataSourceConfigMapper.findAllPublished();
    }

    @Override
    public DataSourceConfig findByVersionId(String versionId) {
        return dataSourceConfigMapper.findByVersionId(versionId);
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String status, String stage) {
        // 更新版本状态
        dataSourceConfigMapper.updateVersionStatus(versionId, status);
        
        // 如果是发布状态且指定了灰度阶段，则插入灰度发布记录
        if (ConfigStatus.PUBLISHED.name().equals(status) && stage != null) {
            dataSourceConfigMapper.insertGrayRelease(versionId, stage);
        }
    }

    @Override
    public List<DataSourceConfig> getActiveByRegion(String region) {
        String stage = regionProvider.getStageByRegion(region);
        return dataSourceConfigMapper.findByStage(stage);
    }

    @Override
    public List<DataSourceConfig> getPublishedByIdentifier(String identifier) {
        return dataSourceConfigMapper.findPublishedConfigsBySource(identifier);
    }

    @Override
    public DataSourceConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        String stage = regionProvider.getStageByRegion(region);
        return dataSourceConfigMapper.findActiveConfigBySourceAndStage(identifier, stage);
    }

    /**
     * 清理旧版本
     */
    private void cleanupOldVersions(String source) {
        dataSourceConfigMapper.deleteBySourceAndStatus(source, ConfigStatus.DRAFT.name());
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse getConfigDiff(ConfigDiffRequest request) {
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
        return ConfigDiffResponse.builder()
            .updatedConfigs(updatedConfigs)
            .deprecatedVersionIds(deprecatedVersionIds)
            .build();
    }
} 