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
        List<DataSourceConfig> existingConfigs = getPublishedBySource(config.getSource());
        if (!existingConfigs.isEmpty()) {
            throw new RuntimeException("Already exists config with source: " + config.getSource());
        }
        
        config.setVersionId(versionGenerator.generateDataSourceVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(config);
        
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
        
        newConfig.setVersionId(versionGenerator.generateDataSourceVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(newConfig);
        
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
    public void updateStatus(String versionId, String status, String grayGroups) {
        dataSourceConfigMapper.updateStatus(versionId, status, grayGroups);
    }

    @Override
    public List<DataSourceConfig> getActiveByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findByRegion(region);
    }

    @Override
    public List<DataSourceConfig> getPublishedByIdentifier(String identifier) {
        return dataSourceConfigMapper.findPublishedConfigsBySource(identifier);
    }
    
    @Override
    public DataSourceConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findActiveConfigBySourceAndRegion(identifier, region);
    }

    /**
     * 获取指定source的所有已发布配置
     */
    public List<DataSourceConfig> getPublishedBySource(String source) {
        return dataSourceConfigMapper.findPublishedConfigsBySource(source);
    }

    /**
     * 获取指定source在指定地域生效的配置
     */
    public DataSourceConfig getActiveBySourceAndRegion(String source, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findActiveConfigBySourceAndRegion(source, region);
    }

    /**
     * 清理过期版本
     * 保留最新的N个版本，其他的删除
     */
    private void cleanupOldVersions(String source) {
        List<DataSourceConfig> allVersions = dataSourceConfigMapper.findAllVersionsBySource(source);
        if (allVersions.size() > versionProperties.getMaxDatasourceVersions()) {
            // 跳过最新的N个版本，删除剩余的版本
            allVersions.stream()
                .skip(versionProperties.getMaxDatasourceVersions())
                .forEach(config -> dataSourceConfigMapper.deleteByVersionId(config.getVersionId()));
        }
    }

    /**
     * 获取配置变更信息
     */
    public ConfigDiffResponse getConfigDiff(ConfigDiffRequest request) {
        if (!regionProvider.isRegionSupported(request.getRegion())) {
            throw new IllegalArgumentException("Unsupported region: " + request.getRegion());
        }
        
        // 获取当前所有生效的配置
        List<DataSourceConfig> currentConfigs = dataSourceConfigMapper.findActiveConfigsByRegion(request.getRegion());
        
        // 获取已失效的版本
        List<String> deprecatedVersionIds = dataSourceConfigMapper.findDeprecatedVersions(
            request.getVersionIds(), 
            request.getRegion()
        );
        
        // 找出新增或更新的配置
        Set<String> oldVersions = new HashSet<>(request.getVersionIds());
        List<DataSourceConfig> updatedConfigs = currentConfigs.stream()
            .filter(config -> !oldVersions.contains(config.getVersionId()))
            .collect(Collectors.toList());
        
        // 构建响应
        ConfigDiffResponse response = new ConfigDiffResponse();
        response.setUpdatedConfigs(updatedConfigs);
        response.setDeprecatedVersionIds(deprecatedVersionIds);
        
        return response;
    }
} 