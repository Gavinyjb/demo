package com.example.service.impl;

import com.example.enums.ConfigStatus;
import com.example.mapper.DataSourceConfigMapper;
import com.example.model.DataSourceConfig;
import com.example.service.DataSourceConfigService;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataSourceConfigServiceImpl implements DataSourceConfigService {
    
    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;

    @Override
    @Transactional
    public DataSourceConfig create(DataSourceConfig config) {
        // 检查是否已存在相同标识的配置
        List<DataSourceConfig> existingConfigs = getPublishedBySource(config.getSource());
        if (!existingConfigs.isEmpty()) {
            // 可以根据业务需求决定是抛出异常还是其他处理
            throw new RuntimeException("Already exists config with source: " + config.getSource());
        }
        
        config.setVersionId(versionGenerator.generateDataSourceVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public DataSourceConfig update(String oldVersionId, DataSourceConfig newConfig) {
        DataSourceConfig oldConfig = dataSourceConfigMapper.findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateDataSourceVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        dataSourceConfigMapper.insert(newConfig);
        return newConfig;
    }

    @Override
    public List<DataSourceConfig> getAllPublished() {
        return dataSourceConfigMapper.findAllPublished();
    }

    @Override
    public List<DataSourceConfig> getActiveByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findByRegion(region);
    }

    @Override
    public DataSourceConfig getActiveBySourceAndRegion(String source, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return dataSourceConfigMapper.findActiveConfigBySourceAndRegion(source, region);
    }

    @Override
    public List<DataSourceConfig> getPublishedBySource(String source) {
        return dataSourceConfigMapper.findPublishedConfigsBySource(source);
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String status, String grayGroups) {
        dataSourceConfigMapper.updateStatus(versionId, status, grayGroups);
    }
} 