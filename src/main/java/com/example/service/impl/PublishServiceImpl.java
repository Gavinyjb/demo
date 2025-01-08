package com.example.service.impl;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.PublishHistory;
import com.example.service.PublishService;
import com.example.service.DataSourceConfigService;
import com.example.service.ApiRecordConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishServiceImpl implements PublishService {
    @Autowired
    private DataSourceConfigService dataSourceConfigService;
    
    @Autowired
    private ApiRecordConfigService apiRecordConfigService;
    
    @Autowired
    private PublishHistoryMapper publishHistoryMapper;

    @Override
    @Transactional
    public void publish(String versionId, String configType, List<String> grayGroups, String operator) {
        String grayGroupsJson = String.join(",", grayGroups);
        
        if (ConfigType.DATA_SOURCE.name().equals(configType)) {
            dataSourceConfigService.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        } else if (ConfigType.API_RECORD.name().equals(configType)) {
            apiRecordConfigService.updateStatus(versionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        }
        
        recordHistory(versionId, configType, ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
    }
    
    @Override
    @Transactional
    public void publishByStage(String versionId, String configType, GrayStage stage, String operator) {
        publish(versionId, configType, stage.getRegions(), operator);
    }
    
    @Override
    @Transactional
    public void rollback(String currentVersionId, String targetVersionId, List<String> grayGroups, String operator) {
        String grayGroupsJson = String.join(",", grayGroups);
        
        // 处理数据源配置回滚
        dataSourceConfigService.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
        dataSourceConfigService.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        recordHistory(targetVersionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
        
        // 处理API记录配置回滚
        apiRecordConfigService.updateStatus(currentVersionId, ConfigStatus.DEPRECATED.name(), "");
        apiRecordConfigService.updateStatus(targetVersionId, ConfigStatus.PUBLISHED.name(), grayGroupsJson);
        recordHistory(targetVersionId, ConfigType.API_RECORD.name(), ConfigStatus.PUBLISHED.name(), grayGroupsJson, operator);
    }
    
    @Override
    @Transactional
    public void deprecate(String versionId, List<String> grayGroups, String operator) {
        String grayGroupsJson = String.join(",", grayGroups);
        
        dataSourceConfigService.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), grayGroupsJson);
        apiRecordConfigService.updateStatus(versionId, ConfigStatus.DEPRECATED.name(), grayGroupsJson);
        
        recordHistory(versionId, ConfigType.DATA_SOURCE.name(), ConfigStatus.DEPRECATED.name(), grayGroupsJson, operator);
        recordHistory(versionId, ConfigType.API_RECORD.name(), ConfigStatus.DEPRECATED.name(), grayGroupsJson, operator);
    }
    
    private void recordHistory(String versionId, String configType, String status, String grayGroups, String operator) {
        PublishHistory history = new PublishHistory();
        history.setVersionId(versionId);
        history.setConfigType(configType);
        history.setStatus(status);
        history.setGrayGroups(grayGroups);
        history.setOperator(operator);
        publishHistoryMapper.insert(history);
    }
} 