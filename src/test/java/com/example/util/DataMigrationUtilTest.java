package com.example.util;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.mapper.*;
import com.example.model.ConfigVersion;
import com.example.model.PublishHistory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class DataMigrationUtilTest {

    @Autowired
    private DataMigrationUtil dataMigrationUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigVersionMapper configVersionMapper;

    @Autowired
    private ConfigGrayReleaseMapper configGrayReleaseMapper;

    @Autowired
    private PublishHistoryMapper publishHistoryMapper;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        jdbcTemplate.execute("INSERT INTO conf_data_source_config (" +
            "name, source_group, gateway_type, dm, " +
            "sls_region_id, sls_endpoint, sls_project, sls_log_store, " +
            "sls_account_id, sls_role_arn, sls_cursor, " +
            "consume_region, consumer_group_name, status, worker_config, comment" +
            ") VALUES (" +
            "'test-source-1', 'group1', 'API', 'data', " +
            "'cn-hangzhou', 'endpoint1', 'project1', 'store1', " +
            "'account1', 'role1', 'cursor1', " +
            "'cn-hangzhou', 'consumer1', 1, '{}', 'test1'" +
            ")");

        jdbcTemplate.execute("INSERT INTO conf_data_source_config (" +
            "name, source_group, gateway_type, dm, " +
            "sls_region_id, sls_endpoint, sls_project, sls_log_store, " +
            "sls_account_id, sls_role_arn, sls_cursor, " +
            "consume_region, consumer_group_name, status, worker_config, comment" +
            ") VALUES (" +
            "'test-source-2', 'group2', 'API', 'data', " +
            "'cn-shanghai', 'endpoint2', 'project2', 'store2', " +
            "'account2', 'role2', 'cursor2', " +
            "'cn-shanghai', 'consumer2', 1, '{}', 'test2'" +
            ")");
    }

    @Test
    void testMigrateDataSourceConfig() {
        // 1. 执行迁移
        dataMigrationUtil.migrateDataSourceConfig();

        // 2. 验证数据源配置是否都有version_id
        int unmigratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config WHERE version_id IS NULL",
            Integer.class
        );
        assertEquals(0, unmigratedCount, "所有记录都应该有version_id");

        // 3. 验证版本记录
        List<ConfigVersion> versions = configVersionMapper.findByIdentifierAndTypeAndStatus(
            "test-source-1",
            ConfigType.DATA_SOURCE.name(),
            ConfigStatus.PUBLISHED.name()
        );
        assertFalse(versions.isEmpty(), "应该存在版本记录");
        
        ConfigVersion version = versions.get(0);
        assertEquals(ConfigType.DATA_SOURCE.name(), version.getConfigType());
        assertEquals(ConfigStatus.PUBLISHED.name(), version.getConfigStatus());

        // 4. 验证灰度发布记录
        String stage = configGrayReleaseMapper.findStageByVersionId(version.getVersionId());
        assertEquals(GrayStage.FULL.name(), stage, "应该是全量发布状态");

        // 5. 验证发布历史
        List<PublishHistory> histories = publishHistoryMapper.findByVersionId(version.getVersionId());
        assertFalse(histories.isEmpty(), "应该存在发布历史");
        
        PublishHistory history = histories.get(0);
        assertEquals(ConfigType.DATA_SOURCE.name(), history.getConfigType());
        assertEquals(ConfigStatus.PUBLISHED.name(), history.getConfigStatus());
        assertEquals(GrayStage.FULL.name(), history.getStage());
        assertEquals("system_migration", history.getOperator());
    }

    @Test
    void testValidateMigration() {
        // 1. 先执行迁移
        dataMigrationUtil.migrateDataSourceConfig();

        // 2. 执行验证
        dataMigrationUtil.validateMigration();

        // 3. 检查是否有未迁移的记录
        int unmigratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config WHERE version_id IS NULL",
            Integer.class
        );
        assertEquals(0, unmigratedCount, "不应该有未迁移的记录");

        // 4. 检查版本记录是否完整
        int missingVersions = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config t " +
            "LEFT JOIN config_version v ON t.version_id = v.version_id " +
            "WHERE v.version_id IS NULL AND t.version_id IS NOT NULL",
            Integer.class
        );
        assertEquals(0, missingVersions, "不应该有缺失的版本记录");

        // 5. 检查发布历史是否完整
        int missingHistory = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config t " +
            "LEFT JOIN publish_history h ON t.version_id = h.version_id " +
            "WHERE h.version_id IS NULL AND t.version_id IS NOT NULL",
            Integer.class
        );
        assertEquals(0, missingHistory, "不应该有缺失的发布历史");
    }

    @Test
    void testBatchProcessing() {
        // 1. 插入大量测试数据
        for (int i = 0; i < 250; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO conf_data_source_config (" +
                "name, source_group, gateway_type, dm) VALUES (" +
                "'test-source-%d', 'group1', 'API', 'data')",
                i + 3
            ));
        }

        // 2. 执行迁移
        dataMigrationUtil.migrateDataSourceConfig();

        // 3. 验证所有记录都已迁移
        int totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config",
            Integer.class
        );
        int migratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config WHERE version_id IS NOT NULL",
            Integer.class
        );
        assertEquals(totalCount, migratedCount, "所有记录都应该已迁移");
    }
} 