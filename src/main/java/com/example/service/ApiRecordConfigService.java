package com.example.service;

import com.example.model.ApiRecordConfig;
import java.util.List;

/**
 * API记录配置服务接口
 */
public interface ApiRecordConfigService {
    /**
     * 创建API记录配置
     */
    ApiRecordConfig create(ApiRecordConfig config);
    
    /**
     * 更新API记录配置
     */
    ApiRecordConfig update(String oldVersionId, ApiRecordConfig newConfig);
    
    /**
     * 获取所有已发布的配置
     */
    List<ApiRecordConfig> getAllPublished();
    
    /**
     * 获取指定地域生效的配置
     */
    List<ApiRecordConfig> getActiveByRegion(String region);
    
    /**
     * 获取指定API在指定地域生效的配置
     */
    ApiRecordConfig getActiveByIdentifierAndRegion(String gatewayType, String gatewayCode, 
                                                 String apiVersion, String apiName, String region);
    
    /**
     * 获取指定API的所有已发布配置
     */
    List<ApiRecordConfig> getPublishedByIdentifier(String gatewayType, String gatewayCode, 
                                                  String apiVersion, String apiName);
    
    /**
     * 更新配置状态
     */
    void updateStatus(String versionId, String status, String grayGroups);
} 