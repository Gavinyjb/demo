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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class ApiMetaMigrationTest {

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
        jdbcTemplate.execute("INSERT INTO amp_api_meta (" +
            "api_name, product, gateway_type, dm, gateway_code, api_version, " +
            "actiontrail_code, operation_type, description, visibility, " +
            "isolation_type, service_type, response_body_log, invoke_type, " +
            "resource_spec, effective_flag, audit_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "'test-api-1', 'test-product', 'API', 'data', 'test-gateway-1', '1.0.0', " +
            "'action-1', 'READ', 'test description 1', 'PUBLIC', " +
            "'TENANT', 'HTTP', 1, 'SYNC', " +
            "'{\"cpu\":1,\"memory\":2048}', 'Y', 'ENABLED', " +
            "NOW(), NOW()" +
            ")");

        jdbcTemplate.execute("INSERT INTO amp_api_meta (" +
            "api_name, product, gateway_type, dm, gateway_code, api_version, " +
            "actiontrail_code, operation_type, description, visibility, " +
            "isolation_type, service_type, response_body_log, invoke_type, " +
            "resource_spec, effective_flag, audit_status, " +
            "gmt_create, gmt_modified" +
            ") VALUES (" +
            "'test-api-2', 'test-product', 'API', 'data', 'test-gateway-2', '2.0.0', " +
            "'action-2', 'WRITE', 'test description 2', 'PRIVATE', " +
            "'TENANT', 'HTTP', 0, 'ASYNC', " +
            "'{\"cpu\":2,\"memory\":4096}', 'Y', 'ENABLED', " +
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
            "SELECT COUNT(*) FROM amp_api_meta",
            Integer.class
        );
        int unmigratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM amp_api_meta WHERE version_id IS NULL",
            Integer.class
        );
        assertEquals(totalCount, unmigratedCount, "初始状态下所有记录的version_id都应该为NULL");

        // 2. 验证没有相关的版本记录
        int versionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM config_version WHERE config_type = ?",
            Integer.class,
            ConfigType.API_META.name()
        );
        assertEquals(0, versionCount, "初始状态下不应该有版本记录");

        // 3. 验证没有相关的发布历史
        int historyCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM publish_history WHERE config_type = ?",
            Integer.class,
            ConfigType.API_META.name()
        );
        assertEquals(0, historyCount, "初始状态下不应该有发布历史");

        log.info("初始状态验证通过: 总记录数={}, 未迁移记录数={}", totalCount, unmigratedCount);
    }

    @Test
    void testBatchProcessing() {
        // 1. 插入大量测试数据
        for (int i = 0; i < 250; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO amp_api_meta (" +
                "api_name, product, gateway_type, dm, gateway_code, api_version, " +
                "actiontrail_code, operation_type, description, visibility, " +
                "isolation_type, service_type, response_body_log, invoke_type, " +
                "resource_spec, effective_flag, audit_status, " +
                "gmt_create, gmt_modified" +
                ") VALUES (" +
                "'test-api-%d', 'test-product', 'API', 'data', 'test-gateway-%d', '1.0.0', " +
                "'action-%d', 'READ', 'test description', 'PUBLIC', " +
                "'TENANT', 'HTTP', 1, 'SYNC', " +
                "'{\"cpu\":1,\"memory\":2048}', 'Y', 'ENABLED', " +
                "NOW(), NOW()" +
                ")",
                i + 3, i + 3, i + 3
            ));
        }

        // 2. 执行迁移
        dataMigrationUtil.migrateApiMetaConfig();

        // 3. 验证所有记录都已迁移
        int totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM amp_api_meta",
            Integer.class
        );
        int migratedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM amp_api_meta WHERE version_id IS NOT NULL",
            Integer.class
        );
        assertEquals(totalCount, migratedCount, "所有记录都应该已迁移");

        // 4. 验证批处理是否正确执行
        int batchCount = totalCount / DataMigrationUtil.BATCH_SIZE + (totalCount % DataMigrationUtil.BATCH_SIZE > 0 ? 1 : 0);
        log.info("总记录数: {}, 批次数: {}", totalCount, batchCount);
    }
} 