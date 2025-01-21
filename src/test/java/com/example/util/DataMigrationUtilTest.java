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
import org.springframework.test.annotation.Rollback;
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
            "consume_region, consumer_group_name, status, worker_config, comment, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "'test-source-1', 'group1', 'API', 'data', " +
            "'cn-hangzhou', 'endpoint1', 'project1', 'store1', " +
            "'account1', 'role1', 'cursor1', " +
            "'cn-hangzhou', 'consumer1', 1, '{}', 'test1', " +
            "NOW(), NOW()" +
            ")");

        jdbcTemplate.execute("INSERT INTO conf_data_source_config (" +
            "name, source_group, gateway_type, dm, " +
            "sls_region_id, sls_endpoint, sls_project, sls_log_store, " +
            "sls_account_id, sls_role_arn, sls_cursor, " +
            "consume_region, consumer_group_name, status, worker_config, comment, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "'test-source-2', 'group2', 'API', 'data', " +
            "'cn-shanghai', 'endpoint2', 'project2', 'store2', " +
            "'account2', 'role2', 'cursor2', " +
            "'cn-shanghai', 'consumer2', 1, '{}', 'test2', " +
            "NOW(), NOW()" +
            ")");

        // 验证初始状态
        validateInitialState();
    }

    /**
     * 验证初始状态
     */
    private void validateInitialState() {
        // 1. 验证所有记录都没有 version_id
        int totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config",
            Integer.class
        );
        int unmigratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM conf_data_source_config WHERE version_id IS NULL",
            Integer.class
        );
        assertEquals(totalCount, unmigratedCount, "初始状态下所有记录的version_id都应该为NULL");

        // 2. 验证没有相关的版本记录
        int versionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM config_version WHERE config_type = ?",
            Integer.class,
            ConfigType.DATA_SOURCE.name()
        );
        assertEquals(0, versionCount, "初始状态下不应该有版本记录");

        // 3. 验证没有相关的发布历史
        int historyCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM publish_history WHERE config_type = ?",
            Integer.class,
            ConfigType.DATA_SOURCE.name()
        );
        assertEquals(0, historyCount, "初始状态下不应该有发布历史");

        // 4. 验证没有相关的灰度发布记录
        int grayReleaseCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM config_gray_release",
            Integer.class
        );
        assertEquals(0, grayReleaseCount, "初始状态下不应该有灰度发布记录");

        log.info("初始状态验证通过: 总记录数={}, 未迁移记录数={}", totalCount, unmigratedCount);
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
    @Rollback(false)
    void testBatchProcessing() {
        // 1. 插入大量测试数据
        for (int i = 0; i < 250; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO conf_data_source_config (" +
                "name, source_group, gateway_type, dm, " +
                "sls_region_id, sls_endpoint, sls_project, sls_log_store, " +
                "sls_account_id, sls_role_arn, sls_cursor, " +
                "consume_region, consumer_group_name, status, worker_config, comment, " +
                "gmt_create, gmt_modified" +
                ") VALUES (" +
                "'test-source-%d', 'group1', 'API', 'data', " +
                "'cn-hangzhou', 'endpoint1', 'project1', 'store1', " +
                "'account1', 'role1', 'cursor1', " +
                "'cn-hangzhou', 'consumer1', 1, '{}', 'test comment', " +
                "NOW(), NOW()" +
                ")",
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

        // 4. 验证批处理是否正确执行
        int batchCount = totalCount / DataMigrationUtil.BATCH_SIZE + (totalCount % DataMigrationUtil.BATCH_SIZE > 0 ? 1 : 0);
        log.info("总记录数: {}, 批次数: {}", totalCount, batchCount);
    }
} 