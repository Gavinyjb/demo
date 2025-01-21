package com.example.service;

import com.example.enums.ConfigStatus;
import com.example.enums.GrayStage;
import com.example.mapper.ApiRecordConfigMapper;
import com.example.model.bo.ApiRecordConfigBO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
public class ApiRecordConfigServiceTest {

    @Autowired
    private ApiRecordConfigService apiRecordConfigService;
    
    @Autowired
    private PublishService publishService;

    @Autowired
    private ApiRecordConfigMapper apiRecordConfigMapper;
    
    @Test
    public void testConfigLifecycle() {
        // 1. 创建配置
        ApiRecordConfigBO config = createTestConfig("test-api");
        ApiRecordConfigBO created = apiRecordConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());
        
        // 2. 灰度发布到 STAGE_1
        publishService.publishByStage(
            created.getVersionId(),
            "API_RECORD",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 验证 STAGE_1 地域可以查询到配置
        ApiRecordConfigBO stage1Config = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1Config);
        assertEquals(created.getVersionId(), stage1Config.getVersionId());
        
        // 验证 STAGE_2 地域查询不到配置
        ApiRecordConfigBO otherConfig = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "cn-chengdu"
        );
        assertNull(otherConfig);

        //全量发布
        publishService.publishByStage(
            created.getVersionId(),
            "API_RECORD",
            GrayStage.FULL,
            "test-user"
        );
        
        // 3. 创建新版本
        ApiRecordConfigBO newConfig = createTestConfig("test-api");
        ApiRecordConfigBO updated = apiRecordConfigService.update(
            created.getVersionId(),
            newConfig
        );
        
        // 4. 新版本灰度发布到 STAGE_1
        publishService.publishByStage(
            updated.getVersionId(),
            "API_RECORD",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 验证 STAGE_1 地域可以查询到新版本，STAGE_2 地域查询到旧版本
        ApiRecordConfigBO stage1ConfigAfterUpdate = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1ConfigAfterUpdate);
        assertEquals(updated.getVersionId(), stage1ConfigAfterUpdate.getVersionId());

        ApiRecordConfigBO stage2ConfigAfterUpdate = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created.getIdentifier(),
            "cn-chengdu"
        );
        assertNotNull(stage2ConfigAfterUpdate);
        assertEquals(created.getVersionId(), stage2ConfigAfterUpdate.getVersionId());

        //全量发布
        publishService.publishByStage(
            updated.getVersionId(),
            "API_RECORD",
            GrayStage.FULL,
            "test-user"
        );

        // 验证旧版本被废弃
        ApiRecordConfigBO oldConfig = apiRecordConfigService.findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), oldConfig.getConfigStatus());
    }
    
    @Test
    public void testCannotPublishMultipleGrayingConfigs() {
        // 1. 创建配置
        ApiRecordConfigBO config = createTestConfig("test-api");
        ApiRecordConfigBO created = apiRecordConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());
        
        // 2. 灰度发布到 STAGE_1
        publishService.publishByStage(
            created.getVersionId(),
            "API_RECORD",
            GrayStage.STAGE_1,
            "test-user"
        );
        
        // 3. 创建新配置
        ApiRecordConfigBO newConfig = createTestConfig("test-api");
        ApiRecordConfigBO created2 = apiRecordConfigService.create(newConfig);
        
        // 4. 尝试发布新配置到 STAGE_1，应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            publishService.publishByStage(
                created2.getVersionId(),
                "API_RECORD",
                GrayStage.STAGE_1,
                "test-user"
            );
        });
    }

    @Test
    public void testPublishRetryOnDuplicateKey() {
        // 1. 创建配置
        ApiRecordConfigBO config = createTestConfig("test-api");
        ApiRecordConfigBO created = apiRecordConfigService.create(config);
        assertNotNull(created.getVersionId());
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());

        // 2. 快速连续发布到不同阶段
        publishService.publishByStage(
            created.getVersionId(),
            "API_RECORD",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 立即发布到全量，应该会触发重试逻辑
        publishService.publishByStage(
            created.getVersionId(),
            "API_RECORD",
            GrayStage.FULL,
            "test-user"
        );

        // 验证最终发布成功
        ApiRecordConfigBO publishedConfig = apiRecordConfigService
            .findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.PUBLISHED.name(), publishedConfig.getConfigStatus());
    }

    @Test
    public void testRollbackGrayConfig() throws InterruptedException {
        // 1. 创建配置并发布到全量
        ApiRecordConfigBO config1 = createTestConfig("test-api");
        ApiRecordConfigBO created1 = apiRecordConfigService.create(config1);
        publishService.publishByStage(
            created1.getVersionId(),
            "API_RECORD",
            GrayStage.FULL,
            "test-user"
        );

        // 2. 更新配置并灰度发布到 STAGE_1
        ApiRecordConfigBO config2 = createTestConfig("test-api");
        ApiRecordConfigBO updated = apiRecordConfigService.update(
            created1.getVersionId(), 
            config2
        );
        publishService.publishByStage(
            updated.getVersionId(),
            "API_RECORD",
            GrayStage.STAGE_1,
            "test-user"
        );

        // 3. 验证 STAGE_1 地域可以查询到新版本
        ApiRecordConfigBO stage1Config = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1Config);
        assertEquals(updated.getVersionId(), stage1Config.getVersionId());

        //STAGE_2 地域查询到旧版本
        ApiRecordConfigBO stage2Config = apiRecordConfigService.getActiveByIdentifierAndRegion(
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
            "API_RECORD",
            "test-user"
        );

        // 5. 验证所有地域都查询到旧版本
        ApiRecordConfigBO stage1ConfigAfterRollback = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "ap-southeast-2"
        );
        assertNotNull(stage1ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage1ConfigAfterRollback.getVersionId());

        ApiRecordConfigBO stage2ConfigAfterRollback = apiRecordConfigService.getActiveByIdentifierAndRegion(
            created1.getIdentifier(),
            "cn-chengdu"
        );
        assertNotNull(stage2ConfigAfterRollback);
        assertEquals(created1.getVersionId(), stage2ConfigAfterRollback.getVersionId());

        // 6. 验证灰度版本已被废弃
        ApiRecordConfigBO rolledBackConfig = apiRecordConfigService.findByVersionId(updated.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), rolledBackConfig.getConfigStatus());
    }
    
    private ApiRecordConfigBO createTestConfig(String apiName) {
        ApiRecordConfigBO config = new ApiRecordConfigBO();
        config.setApiName(apiName);
        config.setGatewayType("API");
        config.setGatewayCode("test-gateway");
        config.setApiVersion("1.0.0");
        config.setBasicConfig("{}");  // 设置一个空的 JSON 对象作为基础配置
        return config;
    }
} 