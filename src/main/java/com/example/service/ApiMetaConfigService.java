package com.example.service;

import com.example.model.ApiMetaConfig;
import com.example.mapper.ApiMetaConfigMapper;
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
 * API Meta配置服务
 */
@Service
public class ApiMetaConfigService implements BaseConfigService<ApiMetaConfig> {
    
    @Autowired
    private ApiMetaConfigMapper apiMetaConfigMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;
    
    @Autowired
    private RegionProvider regionProvider;
    
    @Autowired
    private VersionProperties versionProperties;

    @Override
    @Transactional
    public ApiMetaConfig create(ApiMetaConfig config) {
        if (hasSameApiMetaConfig(config)) {
            throw new RuntimeException("Already exists config with identifier: " + config.getIdentifier());
        }
        
        config.setVersionId(versionGenerator.generateApiMetaVersion());
        config.setStatus(ConfigStatus.DRAFT.name());
        apiMetaConfigMapper.insert(config);
        
        cleanupOldVersions(config);
        
        return config;
    }

    @Override
    @Transactional
    public ApiMetaConfig update(String oldVersionId, ApiMetaConfig newConfig) {
        ApiMetaConfig oldConfig = findByVersionId(oldVersionId);
        if (oldConfig == null) {
            throw new RuntimeException("原配置版本不存在");
        }
        
        newConfig.setVersionId(versionGenerator.generateApiMetaVersion());
        newConfig.setStatus(ConfigStatus.DRAFT.name());
        apiMetaConfigMapper.insert(newConfig);
        
        cleanupOldVersions(newConfig);
        
        return newConfig;
    }

    @Override
    public List<ApiMetaConfig> getAllPublished() {
        return apiMetaConfigMapper.findAllPublished();
    }

    @Override
    public ApiMetaConfig findByVersionId(String versionId) {
        return apiMetaConfigMapper.findByVersionId(versionId);
    }

    @Override
    @Transactional
    public void updateStatus(String versionId, String status, String grayGroups) {
        apiMetaConfigMapper.updateStatus(versionId, status, grayGroups);
    }

    @Override
    public List<ApiMetaConfig> getActiveByRegion(String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return apiMetaConfigMapper.findByRegion(region);
    }

    @Override
    public List<ApiMetaConfig> getPublishedByIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getPublishedByIdentifier(parts[0], parts[1], parts[2], parts[3]);
    }

    @Override
    public ApiMetaConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getActiveByIdentifierAndRegion(parts[0], parts[1], parts[2], parts[3], region);
    }

    /**
     * 获取指定API的所有已发布配置
     */
    public List<ApiMetaConfig> getPublishedByIdentifier(String gatewayType, String gatewayCode, 
                                                      String apiVersion, String apiName) {
        return apiMetaConfigMapper.findPublishedConfigsByIdentifier(
            gatewayType, gatewayCode, apiVersion, apiName);
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    public ApiMetaConfig getActiveByIdentifierAndRegion(String gatewayType, String gatewayCode, 
                                                      String apiVersion, String apiName, String region) {
        if (!regionProvider.isRegionSupported(region)) {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }
        return apiMetaConfigMapper.findActiveConfigByIdentifierAndRegion(
            gatewayType, gatewayCode, apiVersion, apiName, region);
    }

    private boolean hasSameApiMetaConfig(ApiMetaConfig config) {
        List<ApiMetaConfig> existingConfigs = getAllPublished();
        return ConfigIdentifierUtils.hasSameIdentifier(existingConfigs, config);
    }

    private void cleanupOldVersions(ApiMetaConfig config) {
        List<ApiMetaConfig> allVersions = apiMetaConfigMapper.findAllVersionsByIdentifier(
            config.getGatewayType(),
            config.getGatewayCode(),
            config.getApiVersion(),
            config.getApiName()
        );
        
        if (allVersions.size() > versionProperties.getMaxApiMetaVersions()) {
            allVersions.stream()
                .skip(versionProperties.getMaxApiMetaVersions())
                .forEach(c -> apiMetaConfigMapper.deleteByVersionId(c.getVersionId()));
        }
    }
} 