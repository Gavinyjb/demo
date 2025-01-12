package com.example.service;

import com.example.mapper.DataSourceConfigMapper;
import com.example.mapper.PublishHistoryMapper;
import com.example.model.DataSourceConfig;
import com.example.util.VersionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DataMigrationService {
    
    @Autowired
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Autowired
    private PublishHistoryMapper publishHistoryMapper;
    
    @Autowired
    private VersionGenerator versionGenerator;

    /**
     * 迁移存量数据源配置
     */
    @Transactional
    public void migrateDataSourceConfigs() {
        // 1. 获取所有需要迁移的配置
        List<DataSourceConfig> configs = dataSourceConfigMapper.findConfigsWithoutVersion();
        
        // 2. 为每个配置生成版本信息并更新
        for (DataSourceConfig config : configs) {
            // 生成版本ID
            String versionId = versionGenerator.generateDataSourceVersion();
            
            // 更新配置
            dataSourceConfigMapper.updateVersionInfo(
                config.getId(),
                versionId,
                "PUBLISHED",
                "all"
            );
            
            // 记录发布历史
            publishHistoryMapper.insertMigrationHistory(
                versionId,
                "DATA_SOURCE",
                "PUBLISHED",
                "all",
                "system_migration"
            );
        }
    }
} 