package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.GrayStage;
import com.example.mapper.ApiMetaConfigMapper;
import com.example.model.bo.ApiMetaConfigBO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
public class ApiMetaConfigServiceTest {

    @Autowired
    private ApiMetaConfigService apiMetaConfigService;
    
    @Autowired
    private PublishService publishService;

    @Autowired
    private ApiMetaConfigMapper apiMetaConfigMapper;
    
    @Test
//    @Rollback(false)
    public void testConfigLifecycle() {
        // 1. 创建配置
        ApiMetaConfigBO config = createTestConfig("test-api");
        ApiMetaConfigBO created = apiMetaConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());
        
        // 2. 灰度发布到 STAGE_1
        publishService.publishByStage(
            created.getVersionId(),
            "API_META",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 验证 STAGE_1 地域可以查询到配置
        ApiMetaConfigBO stage1Config = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1Config);
        assertEquals(created.getVersionId(), stage1Config.getVersionId());
        
        // 验证 STAGE_2 地域查询不到配置
        ApiMetaConfigBO otherConfig = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "cn-hangzhou"
        );
        assertNull(otherConfig);

        //全量发布
        publishService.publishByStage(
            created.getVersionId(),
            "API_META",
            GrayStage.FULL,
            "test-user"
        );
        
        // 3. 创建新版本
        ApiMetaConfigBO newConfig = createTestConfig("test-api");
        newConfig.setDescription("updated description");
        ApiMetaConfigBO updated = apiMetaConfigService.update(
            created.getVersionId(),
            newConfig
        );
        
        // 4. 新版本灰度发布到 STAGE_1
        publishService.publishByStage(
            updated.getVersionId(),
            "API_META",
            GrayStage.STAGE_1,
            "test-user"
        );

        log.info("created.identifier:{}", created.getIdentifier());
        log.info("updated.identifier:{}", updated.getIdentifier());
        log.info("created.versionId:{}", created.getVersionId());
        log.info("updated.versionId:{}", updated.getVersionId());
        log.info("created.configStatus:{}", created.getConfigStatus());
        log.info("updated.configStatus:{}", updated.getConfigStatus());

        // 验证 STAGE_1 地域可以查询到新版本，STAGE_2 地域查询到旧版本
        ApiMetaConfigBO stage1ConfigAfterUpdate = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1ConfigAfterUpdate);
        assertEquals(updated.getVersionId(), stage1ConfigAfterUpdate.getVersionId());

        ApiMetaConfigBO stage2ConfigAfterUpdate = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "cn-chengdu"
        );
        assertNotNull(stage2ConfigAfterUpdate);
        assertEquals(created.getVersionId(), stage2ConfigAfterUpdate.getVersionId());

        //全量发布
        publishService.publishByStage(
            updated.getVersionId(),
            "API_META",
            GrayStage.FULL,
            "test-user"
        );

        // 验证旧版本被废弃
        ApiMetaConfigBO oldConfig = apiMetaConfigService.findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), oldConfig.getConfigStatus());
    }
    
    @Test
    public void testCannotPublishMultipleGrayingConfigs() {
        // 1. 创建配置
        ApiMetaConfigBO config = createTestConfig("test-api");
        ApiMetaConfigBO created = apiMetaConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());
        
        // 2. 灰度发布到 STAGE_1
        publishService.publishByStage(
            created.getVersionId(),
            "API_META",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 3. 创建新配置
        ApiMetaConfigBO newConfig = createTestConfig("test-api");
        newConfig.setDescription("updated description");
        ApiMetaConfigBO created2 = apiMetaConfigService.create(newConfig);
        
        // 4. 尝试发布新配置到 STAGE_1，应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            publishService.publishByStage(
                created2.getVersionId(),
                "API_META",
                GrayStage.STAGE_1,
                "test-user"
            );
        });
    }

    @Test
    public void testPublishRetryOnDuplicateKey() {
        // 1. 创建配置
        ApiMetaConfigBO config = createTestConfig("test-api");
        ApiMetaConfigBO created = apiMetaConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());

        // 2. 快速连续发布到不同阶段
        publishService.publishByStage(
            created.getVersionId(),
            "API_META",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 立即发布到全量，应该会触发重试逻辑
        publishService.publishByStage(
            created.getVersionId(),
            "API_META",
            GrayStage.FULL,
            "test-user"
        );

        // 验证最终发布成功
        ApiMetaConfigBO publishedConfig = apiMetaConfigService
            .findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.PUBLISHED.name(), publishedConfig.getConfigStatus());
    }

    
    @Test
    public void testRollbackGrayConfig() throws InterruptedException {
        // 1. 创建配置并发布到全量
        ApiMetaConfigBO config1 = createTestConfig("test-api");
        ApiMetaConfigBO created1 = apiMetaConfigService.create(config1);
        publishService.publishByStage(
            created1.getVersionId(),
            "API_META",
            GrayStage.FULL,
            "test-user"
        );

        // 2. 更新配置并灰度发布到 STAGE_1
        ApiMetaConfigBO config2 = createTestConfig("test-api");
        config2.setDescription("updated description");
        ApiMetaConfigBO updated = apiMetaConfigService.update(
            created1.getVersionId(), 
            config2
        );
        publishService.publishByStage(
            updated.getVersionId(),
            "API_META",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 3.  验证 STAGE_1 地域可以查询到新版本
        ApiMetaConfigBO stage1Config = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1Config);
        assertEquals(updated.getVersionId(), stage1Config.getVersionId());

        //STAGE_2 地域查询到旧版本
        ApiMetaConfigBO stage2Config = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "cn-chengdu"
        );
        assertNotNull(stage2Config);
        assertEquals(created1.getVersionId(), stage2Config.getVersionId());

        //等待 1s
        Thread.sleep(1000);

        // 4. 终止灰度发布
        publishService.rollbackGrayConfig(
                created1.getIdentifier(),
            "API_META",
            "test-user"
        );

        // 5. 验证所有地域都查询到旧版本
        ApiMetaConfigBO stage1ConfigAfterRollback = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage1ConfigAfterRollback.getVersionId());

        ApiMetaConfigBO stage2ConfigAfterRollback = apiMetaConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "cn-chengdu"
        );
        assertNotNull(stage2ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage2ConfigAfterRollback.getVersionId());

        // 6. 验证灰度版本已被废弃
        ApiMetaConfigBO rolledBackConfig = apiMetaConfigService.findByVersionId(updated.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), rolledBackConfig.getConfigStatus());
    }
    
    private ApiMetaConfigBO createTestConfig(String apiName) {
        ApiMetaConfigBO config = new ApiMetaConfigBO();
        config.setApiName(apiName);
        config.setGatewayType("API");
        config.setGatewayCode("test-gateway");
        config.setApiVersion("1.0.0");
        config.setProduct("test-product");
        config.setDescription("test description");
        config.setVisibility("PUBLIC");
        config.setIsolationType("TENANT");
        config.setServiceType("HTTP");
        config.setResponseBodyLog(1);
        config.setInvokeType("SYNC");
        config.setEffectiveFlag("Y");
        config.setAuditStatus("APPROVED");
        return config;
    }
} 