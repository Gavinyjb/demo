package com.example.service;

import com.example.model.bo.DataSourceConfigBO;
import com.example.model.config.WorkerConfig;
import com.example.enums.ConfigStatus;
import com.example.enums.GrayStage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DataSourceConfigServiceTest {

    @Autowired
    private DataSourceConfigService dataSourceConfigService;
    
    @Autowired
    private PublishService publishService;

    @Test
//    @Rollback(false)
    public void testConfigLifecycle() {
        // 1. 创建配置
        DataSourceConfigBO config = createTestConfig("test-source");
        DataSourceConfigBO created = dataSourceConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());
        
        // 2. 灰度发布到 STAGE_1
        publishService.publishByStage(
            created.getVersionId(),
            "DATA_SOURCE",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 验证 STAGE_1 地域可以查询到配置
        DataSourceConfigBO stage1Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "ap-southeast-2");
        assertNotNull(stage1Config);
        assertEquals(created.getVersionId(), stage1Config.getVersionId());

        // 验证 STAGE_2 地域查询不到配置
        DataSourceConfigBO stage2Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "cn-chengdu");
        assertNull(stage2Config);

        //全量发布
        publishService.publishByStage(
            created.getVersionId(),
            "DATA_SOURCE",
            GrayStage.FULL,
            "test-user"
        );
        
        // 3. 创建新版本
        DataSourceConfigBO newConfig = createTestConfig("test-source");
        newConfig.getWorkerConfigObject().setFetchIntervalMillis(2000);
        DataSourceConfigBO updated = dataSourceConfigService.update(
            created.getVersionId(), 
            newConfig
        );
        
        // 4. 新版本灰度发布到 STAGE_1
        publishService.publishByStage(
            updated.getVersionId(),
            "DATA_SOURCE",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 验证 STAGE_1 地域可以查询到新版本，STAGE_2 地域查询到旧版本
        DataSourceConfigBO stage01Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "ap-southeast-2");
        assertNotNull(stage01Config);
        assertEquals(updated.getVersionId(), stage01Config.getVersionId());
        
        DataSourceConfigBO stage02Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "cn-chengdu");
        assertNotNull(stage02Config);
        assertEquals(created.getVersionId(), stage02Config.getVersionId());

        //全量发布
        publishService.publishByStage(
            updated.getVersionId(),
            "DATA_SOURCE",
            GrayStage.FULL,
            "test-user"
        );

        // 验证旧版本被废弃
        DataSourceConfigBO oldConfig = dataSourceConfigService
            .findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), oldConfig.getConfigStatus());
    }
    
    @Test
    public void testCannotPublishMultipleGrayingConfigs() {
        // 1. 创建第一个配置并灰度发布
        DataSourceConfigBO config1 = createTestConfig("test-source");
        DataSourceConfigBO created1 = dataSourceConfigService.create(config1);
        publishService.publishByStage(
            created1.getVersionId(),
            "DATA_SOURCE",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 2. 创建第二个配置并尝试灰度发布
        DataSourceConfigBO config2 = createTestConfig("test-source");
        config2.getWorkerConfigObject().setFetchIntervalMillis(2000);
        DataSourceConfigBO created2 = dataSourceConfigService.create(config2);

        // 3. 验证无法同时灰度发布
        assertThrows(IllegalStateException.class, () -> {
            publishService.publishByStage(
                created2.getVersionId(),
                "DATA_SOURCE",
                GrayStage.STAGE_1,
                "test-user"
            );
        });
    }
    
    @Test
    public void testPublishRetryOnDuplicateKey() {
        // 1. 创建配置
        DataSourceConfigBO config = createTestConfig("test-source");
        DataSourceConfigBO created = dataSourceConfigService.create(config);
        
        // 2. 快速连续发布到不同阶段
        publishService.publishByStage(
            created.getVersionId(),
            "DATA_SOURCE",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 立即发布到全量，应该会触发重试逻辑
        publishService.publishByStage(
            created.getVersionId(),
            "DATA_SOURCE",
            GrayStage.FULL,
            "test-user"
        );
        
        // 验证最终发布成功
        DataSourceConfigBO publishedConfig = dataSourceConfigService
            .findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.PUBLISHED.name(), publishedConfig.getConfigStatus());
    }
    
    @Test
    public void testRollbackGrayConfig() throws InterruptedException {
        // 1. 创建配置并发布到全量
        DataSourceConfigBO config1 = createTestConfig("test-source");
        DataSourceConfigBO created1 = dataSourceConfigService.create(config1);
        publishService.publishByStage(
            created1.getVersionId(),
            "DATA_SOURCE",
            GrayStage.FULL,
            "test-user"
        );

        // 2. 更新配置并灰度发布到 STAGE_1
        DataSourceConfigBO config2 = createTestConfig("test-source");
        config2.getWorkerConfigObject().setFetchIntervalMillis(2000);
        DataSourceConfigBO updated = dataSourceConfigService.update(
            created1.getVersionId(), 
            config2
        );
        publishService.publishByStage(
            updated.getVersionId(),
            "DATA_SOURCE",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 3. 验证 STAGE_1 地域可以查询到新版本，STAGE_2 地域查询到旧版本
        DataSourceConfigBO stage1Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "ap-southeast-2");
        assertNotNull(stage1Config);
        assertEquals(updated.getVersionId(), stage1Config.getVersionId());
        
        DataSourceConfigBO stage2Config = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "cn-chengdu");
        assertNotNull(stage2Config);
        assertEquals(created1.getVersionId(), stage2Config.getVersionId());

        //等待 1s
        Thread.sleep(1000);

        // 4. 终止灰度发布
        publishService.rollbackGrayConfig(
            "test-source",
            "DATA_SOURCE",
            "test-user"
        );

        // 5. 验证所有地域都查询到旧版本
        DataSourceConfigBO stage1ConfigAfterRollback = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "ap-southeast-2");
        assertNotNull(stage1ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage1ConfigAfterRollback.getVersionId());
        
        DataSourceConfigBO stage2ConfigAfterRollback = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "cn-chengdu");
        assertNotNull(stage2ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage2ConfigAfterRollback.getVersionId());

        // 6. 验证灰度版本已被废弃
        DataSourceConfigBO rolledBackConfig = dataSourceConfigService
            .findByVersionId(updated.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), rolledBackConfig.getConfigStatus());
    }
    
    private DataSourceConfigBO createTestConfig(String name) {
        DataSourceConfigBO config = new DataSourceConfigBO();
        config.setName(name);
        config.setSourceGroup("test-group");
        config.setGatewayType("API");
        config.setDm("DATA");
        config.setSlsRegionId("cn-hangzhou");
        config.setSlsEndpoint("cn-hangzhou.log.aliyuncs.com");
        config.setSlsProject("test-project");
        config.setSlsLogStore("test-logstore");
        config.setSlsAccountId("123456789");
        config.setStatus(1);
        config.setSlsRoleArn("acs:ram::123456789:role/test-role");
        config.setConsumerGroupName("test-consumer");
        
        WorkerConfig workerConfig = new WorkerConfig();
        workerConfig.setFetchIntervalMillis(1000);
        config.setWorkerConfigObject(workerConfig);
        
        return config;
    }
} 