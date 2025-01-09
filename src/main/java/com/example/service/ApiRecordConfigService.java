package com.example.service;

import com.example.model.ApiRecordConfig;
import com.example.mapper.ApiRecordConfigMapper;
import com.example.enums.ConfigStatus;
import com.example.util.RegionProvider;
import com.example.util.VersionGenerator;
import com.example.util.ConfigIdentifierUtils;
import com.example.config.VersionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
        if (hasSameApiConfig(config)) {
            throw new RuntimeException("Already exists config with identifier: " + config.getIdentifier());
        }
        
        config.setVersionId(versionGenerator.generateApiRecordVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(config);
        
        cleanupOldVersions(config);
        
        return config;
    }

    @Override
    @Transactional
    public ApiRecordConfig update(String oldVersionId, ApiRecordConfig newConfig) {
        ApiRecordConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateApiRecordVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        apiRecordConfigMapper.insert(newConfig);
        
        cleanupOldVersions(newConfig);
        
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
    public List<ApiRecordConfig> getPublishedByIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getPublishedByIdentifier(parts[0], parts[1], parts[2], parts[3]);
    }

    @Override
    public ApiRecordConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getActiveByIdentifierAndRegion(parts[0], parts[1], parts[2], parts[3], region);
    }

    /**
     * 获取指定API的所有已发布配置
     */
    public List<ApiRecordConfig> getPublishedByIdentifier(String gatewayType, String gatewayCode, 
                                                        String apiVersion, String apiName) {
        return apiRecordConfigMapper.findPublishedConfigsByIdentifier(
            gatewayType, gatewayCode, apiVersion, apiName);
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    public ApiRecordConfig getActiveByIdentifierAndRegion(String gatewayType, String gatewayCode, 
                                                        String apiVersion, String apiName, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return apiRecordConfigMapper.findActiveConfigByIdentifierAndRegion(
            gatewayType, gatewayCode, apiVersion, apiName, region);
    }

    private boolean hasSameApiConfig(ApiRecordConfig config) {
        List<ApiRecordConfig> existingConfigs = getAllPublished();
        return ConfigIdentifierUtils.hasSameIdentifier(existingConfigs, config);
    }

    private void cleanupOldVersions(ApiRecordConfig config) {
        List<ApiRecordConfig> allVersions = apiRecordConfigMapper.findAllVersionsByIdentifier(
            config.getGatewayType(),
            config.getGatewayCode(),
            config.getApiVersion(),
            config.getApiName()
        );
        
        if (allVersions.size() > versionProperties.getMaxApirecordVersions()) {
            allVersions.stream()
                .skip(versionProperties.getMaxApirecordVersions())
                .forEach(c -> apiRecordConfigMapper.deleteByVersionId(c.getVersionId()));
        }
    }
} 