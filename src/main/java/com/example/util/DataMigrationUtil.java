package com.example.util;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.*;
import com.example.model.ConfigVersion;
import com.example.model.PublishHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationUtil {

    private final ConfigMigrationMapper configMigrationMapper;
    private final ConfigVersionMapper configVersionMapper;
    private final ConfigGrayReleaseMapper configGrayReleaseMapper;
    private final PublishHistoryMapper publishHistoryMapper;
    private final VersionGenerator versionGenerator;
    
    private static final int BATCH_SIZE = 100;

    /**
     * 迁移数据源配置
     */
    @Transactional
    public void migrateDataSourceConfig() {
        log.info("开始迁移数据源配置...");
        
        List<Map<String, Object>> records = configMigrationMapper
            .findUnmigratedRecords(ConfigType.DATA_SOURCE.getTableName());
        
        log.info("找到 {} 条待迁移的数据源配置", records.size());
        
        for (int i = 0; i < records.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, records.size());
            List<Map<String, Object>> batch = records.subList(i, endIndex);
            
            processBatch(batch, ConfigType.DATA_SOURCE);
            
            log.info("完成第 {}/{} 批数据源配置迁移", endIndex, records.size());
        }
    }

    /**
     * 迁移API元数据配置
     */
    @Transactional
    public void migrateApiMetaConfig() {
        log.info("开始迁移API元数据配置...");
        
        List<Map<String, Object>> records = configMigrationMapper
            .findUnmigratedRecords(ConfigType.API_META.getTableName());
        
        log.info("找到 {} 条待迁移的API元数据配置", records.size());
        
        for (int i = 0; i < records.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, records.size());
            List<Map<String, Object>> batch = records.subList(i, endIndex);
            
            processBatch(batch, ConfigType.API_META);
            
            log.info("完成第 {}/{} 批API元数据配置迁移", endIndex, records.size());
        }
    }

    private void processBatch(List<Map<String, Object>> batch, ConfigType configType) {
        for (Map<String, Object> record : batch) {
            try {
                migrateRecord(record, configType);
            } catch (Exception e) {
                log.error("迁移记录失败: {}", record, e);
            }
        }
    }

    private void migrateRecord(Map<String, Object> record, ConfigType configType) {
        // 1. 生成版本ID
        String versionId = generateVersionId(configType);
        
        // 2. 构建标识符
        String identifier = buildIdentifier(record, configType);
        
        // 3. 插入版本记录
        configVersionMapper.insert(buildConfigVersion(versionId, identifier, configType));
        
        // 4. 更新原记录的version_id
        configMigrationMapper.updateVersionId(
            configType.getTableName(),
            versionId,
            (Long) record.get("id")
        );
        
        // 5. 插入发布历史
        publishHistoryMapper.insert(buildPublishHistory(versionId, configType));
        
        // 6. 插入灰度发布记录（全量发布）
        configGrayReleaseMapper.insert(versionId, GrayStage.FULL.name());
    }

    private String generateVersionId(ConfigType configType) {
        switch (configType) {
            case DATA_SOURCE:
                return versionGenerator.generateDataSourceVersion();
            case API_META:
                return versionGenerator.generateApiMetaVersion();
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    private String buildIdentifier(Map<String, Object> record, ConfigType configType) {
        switch (configType) {
            case DATA_SOURCE:
                return (String) record.get("name");
            case API_META:
                return String.format("%s:%s:%s:%s",
                    record.get("gateway_type"),
                    record.get("gateway_code"),
                    record.get("api_version"),
                    record.get("api_name")
                );
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    private ConfigVersion buildConfigVersion(String versionId, String identifier, ConfigType configType) {
        return ConfigVersion.builder()
            .versionId(versionId)
            .identifier(identifier)
            .configType(configType.name())
            .configStatus(ConfigStatus.PUBLISHED.name())
            .gmtCreate(LocalDateTime.now())
            .gmtModified(LocalDateTime.now())
            .build();
    }

    private PublishHistory buildPublishHistory(String versionId, ConfigType configType) {
        return PublishHistory.builder()
            .versionId(versionId)
            .configType(configType.name())
            .configStatus(ConfigStatus.PUBLISHED.name())
            .stage(GrayStage.FULL.name())
            .operator("system_migration")
            .gmtCreate(LocalDateTime.now())
            .gmtModified(LocalDateTime.now())
            .build();
    }

    /**
     * 验证迁移结果
     */
    public void validateMigration() {
        log.info("开始验证迁移结果...");
        
        validateConfigType(ConfigType.DATA_SOURCE);
        validateConfigType(ConfigType.API_META);
        
        log.info("迁移验证完成");
    }

    private void validateConfigType(ConfigType configType) {
        String tableName = configType.getTableName();
        
        int unmigratedCount = configMigrationMapper.countUnmigratedRecords(tableName);
        int missingVersions = configMigrationMapper.countMissingVersionRecords(tableName);
        int missingHistory = configMigrationMapper.countMissingHistoryRecords(tableName);
        
        log.info("{}迁移验证结果: 未迁移记录数={}, 缺失版本记录数={}, 缺失历史记录数={}",
            configType.name(), unmigratedCount, missingVersions, missingHistory);
        
        if (unmigratedCount > 0 || missingVersions > 0 || missingHistory > 0) {
            log.warn("{}存在迁移异常", configType.name());
        }
    }
} 