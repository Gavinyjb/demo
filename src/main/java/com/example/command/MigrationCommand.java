package com.example.command;

import com.example.util.DataMigrationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "migration.enabled", havingValue = "true")
public class MigrationCommand implements CommandLineRunner {

    private final DataMigrationUtil dataMigrationUtil;

    @Override
    public void run(String... args) {
        log.info("开始数据迁移...");
        
        try {
            // 1. 迁移数据源配置
            dataMigrationUtil.migrateDataSourceConfig();
            
            // 2. 迁移API元数据配置
            dataMigrationUtil.migrateApiMetaConfig();
            
            // 3. 验证迁移结果
            dataMigrationUtil.validateMigration();
            
            log.info("数据迁移完成");
        } catch (Exception e) {
            log.error("数据迁移失败", e);
            throw e;
        }
    }
} 