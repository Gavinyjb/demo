package com.example.service;

import com.example.model.ApiMetaConfig;
import java.util.List;

/**
 * API Meta配置服务接口
 */
public interface ApiMetaConfigService extends BaseConfigService<ApiMetaConfig> {
    @Override
    default List<ApiMetaConfig> getPublishedByIdentifier(String identifier) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getPublishedByIdentifier(parts[0], parts[1], parts[2], parts[3]);
    }
    
    @Override
    default ApiMetaConfig getActiveByIdentifierAndRegion(String identifier, String region) {
        String[] parts = identifier.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid API identifier format");
        }
        return getActiveByIdentifierAndRegion(parts[0], parts[1], parts[2], parts[3], region);
    }
    
    // 具体实现方法
    List<ApiMetaConfig> getPublishedByIdentifier(String gatewayType, String gatewayCode, 
                                               String apiVersion, String apiName);
    
    ApiMetaConfig getActiveByIdentifierAndRegion(String gatewayType, String gatewayCode, 
                                               String apiVersion, String apiName, String region);
} 