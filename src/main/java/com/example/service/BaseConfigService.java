package com.example.service;

import com.example.model.BaseVersionedConfig;
import com.example.model.ConfigIdentifier;
import java.util.List;

/**
 * 配置服务基础接口
 * @param <T> 配置类型
 */
public interface BaseConfigService<T extends BaseVersionedConfig> {
    /**
     * 创建配置
     */
    T create(T config);
    
    /**
     * 更新配置
     */
    T update(String oldVersionId, T newConfig);
    
    /**
     * 获取所有已发布的配置
     */
    List<T> getAllPublished();
    
    /**
     * 根据版本ID查询配置
     */
    T findByVersionId(String versionId);
    
    /**
     * 更新配置状态和灰度阶段
     */
    void updateStatus(String versionId, String status, String stage);
    
    /**
     * 获取指定地域生效的配置列表
     */
    List<T> getActiveByRegion(String region);
    
    /**
     * 获取指定标识的所有已发布配置
     */
    List<T> getPublishedByIdentifier(String identifier);
    
    /**
     * 获取指定标识在指定地域生效的配置
     */
    T getActiveByIdentifierAndRegion(String identifier, String region);
} 