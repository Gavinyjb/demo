package com.example.service.impl;

import com.example.enums.ConfigStatus;
import com.example.mapper.ApiRecordConfigMapper;
import com.example.model.ApiRecordConfig;
import com.example.service.ApiRecordConfigService;
import com.example.util.VersionGenerator;
import com.example.util.ConfigIdentifierUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApiRecordConfigServiceImpl implements ApiRecordConfigService {
    
    @Autowired
    private ApiRecordConfigMapper apiRecordConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;

    @Override
    @Transactional
    public ApiRecordConfig create(ApiRecordConfig config) {
        // 检查是否已存在相同标识的配置
        if (hasSameApiConfig(config)) {
            throw new RuntimeException("Already exists config with identifier: " + config.getIdentifier());
        }
        
        config.setVersionId(versionGenerator.generateApiRecordVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public ApiRecordConfig update(String oldVersionId, ApiRecordConfig newConfig) {
        ApiRecordConfig oldConfig = apiRecordConfigMapper.findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateApiRecordVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(newConfig);
        return newConfig;
    }

    @Override
    public List<ApiRecordConfig> getAllPublished() {
        return apiRecordConfigMapper.findAllPublished();
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String status, String grayGroups) {
        apiRecordConfigMapper.updateStatus(versionId, status, grayGroups);
    }

    @Override
    public List<ApiRecordConfig> getActiveByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return apiRecordConfigMapper.findByRegion(region);
    }

    @Override
    public ApiRecordConfig getActiveByIdentifierAndRegion(String gatewayType, String gatewayCode, 
                                                        String apiVersion, String apiName, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return apiRecordConfigMapper.findActiveConfigByIdentifierAndRegion(
            gatewayType, gatewayCode, apiVersion, apiName, region);
    }

    @Override
    public List<ApiRecordConfig> getPublishedByIdentifier(String gatewayType, String gatewayCode, 
                                                         String apiVersion, String apiName) {
        return apiRecordConfigMapper.findPublishedConfigsByIdentifier(
            gatewayType, gatewayCode, apiVersion, apiName);
    }

    private boolean hasSameApiConfig(ApiRecordConfig config) {
        List<ApiRecordConfig> existingConfigs = getAllPublished();
        return ConfigIdentifierUtils.hasSameIdentifier(existingConfigs, config);
    }
} 