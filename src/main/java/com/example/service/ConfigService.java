package com.example.service;

import com.example.model.PublishHistory;
import java.util.List;
import com.example.enums.GrayStage;

public interface ConfigService {
    /**
     * 发布配置
     */
    void publishConfig(String versionId, String configType, List<String> grayGroups, String operator);
    
    /**
     * 按灰度阶段发布配置
     */
    void publishConfigByStage(String versionId, String configType, GrayStage stage, String operator);
    
    /**
     * 废弃配置
     */
    void deprecateConfig(String versionId, List<String> grayGroups, String operator);
    
    /**
     * 回滚到上一个版本
     */
    void rollbackToPrevious(String identifier, String configType, String operator);
    
    /**
     * 回滚到指定版本
     */
    void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator);
    
    /**
     * 获取发布历史
     */
    List<PublishHistory> getPublishHistory(String versionId);
} 