package com.example.service;

import com.example.enums.GrayStage;
import com.example.model.PublishHistory;
import java.util.List;

/**
 * 配置发布服务接口
 */
public interface PublishService {
    /**
     * 发布配置
     */
    void publish(String versionId, String configType, List<String> grayGroups, String operator);
    
    /**
     * 按阶段发布配置
     */
    void publishByStage(String versionId, String configType, GrayStage stage, String operator);
    
    /**
     * 回滚配置
     */
    void rollback(String currentVersionId, String targetVersionId, List<String> grayGroups, String operator);

    /**
     * 回滚到上一个版本
     */
    void rollbackToPrevious(String identifier, String configType, String operator);

    /**
     * 回滚到指定版本
     */
    void rollbackToVersion(String identifier, String targetVersionId, String configType, String operator);
    
    /**
     * 废弃配置
     */
    void deprecate(String versionId, List<String> grayGroups, String operator);

    /**
     * 获取发布历史
     */
    List<PublishHistory> getHistory(String versionId);
} 