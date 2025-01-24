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

import java.util.Arrays;
import java.util.List;

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
//
//    @Test
//    void testGetActiveByRegion() {
//        // 1. 创建并发布第一个配置到阶段1
//        ApiRecordConfigBO config1 = createTestConfig("test-api-1");
//        ApiRecordConfigBO created1 = apiRecordConfigService.create(config1);
//        publishService.publishByStage(created1.getVersionId(), "API_RECORD", GrayStage.STAGE_1, "test-user");
//
//        // 2. 创建并发布第二个配置到阶段2
//        ApiRecordConfigBO config2 = createTestConfig("test-api-2");
//        ApiRecordConfigBO created2 = apiRecordConfigService.create(config2);
//        publishService.publishByStage(created2.getVersionId(), "API_RECORD", GrayStage.STAGE_2, "test-user");
//
//        // 3. 创建并发布第三个配置到阶段3
//        ApiRecordConfigBO config3 = createTestConfig("test-api-3");
//        ApiRecordConfigBO created3 = apiRecordConfigService.create(config3);
//        publishService.publishByStage(created3.getVersionId(), "API_RECORD", GrayStage.STAGE_3, "test-user");
//
//        // 4. 验证阶段1地域(马来西亚)可以看到所有配置
//        List<ApiRecordConfigBO> stage1Configs = apiRecordConfigService.getActiveByRegion("ap-southeast-3");
//        assertEquals(3, stage1Configs.size());
//        assertTrue(stage1Configs.stream().anyMatch(c -> c.getVersionId().equals(created1.getVersionId())));
//        assertTrue(stage1Configs.stream().anyMatch(c -> c.getVersionId().equals(created2.getVersionId())));
//        assertTrue(stage1Configs.stream().anyMatch(c -> c.getVersionId().equals(created3.getVersionId())));
//
//        // 5. 验证阶段2地域(成都)可以看到阶段2和阶段1的配置
//        List<ApiRecordConfigBO> stage2Configs = apiRecordConfigService.getActiveByRegion("cn-chengdu");
//        assertEquals(2, stage2Configs.size());
//        assertTrue(stage2Configs.stream().anyMatch(c -> c.getVersionId().equals(created2.getVersionId())));
//        assertTrue(stage2Configs.stream().anyMatch(c -> c.getVersionId().equals(created3.getVersionId())));
//
//        // 6. 验证阶段3地域(日本)只能看到阶段3的配置
//        List<ApiRecordConfigBO> stage3Configs = apiRecordConfigService.getActiveByRegion("ap-northeast-1");
//        assertEquals(1, stage3Configs.size());
//        assertEquals(created3.getVersionId(), stage3Configs.get(0).getVersionId());
//
//        // 7. 创建并发布第四个配置到全量
//        ApiRecordConfigBO config4 = createTestConfig("test-api-4");
//        ApiRecordConfigBO created4 = apiRecordConfigService.create(config4);
//        publishService.publishByStage(created4.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");
//
//        // 8. 验证所有地域都可以看到全量配置
//        List<ApiRecordConfigBO> stage1ConfigsAfterFull = apiRecordConfigService.getActiveByRegion("ap-southeast-3");
//        assertTrue(stage1ConfigsAfterFull.stream().anyMatch(c -> c.getVersionId().equals(created4.getVersionId())));
//
//        List<ApiRecordConfigBO> stage2ConfigsAfterFull = apiRecordConfigService.getActiveByRegion("cn-chengdu");
//        assertTrue(stage2ConfigsAfterFull.stream().anyMatch(c -> c.getVersionId().equals(created4.getVersionId())));
//
//        List<ApiRecordConfigBO> stage3ConfigsAfterFull = apiRecordConfigService.getActiveByRegion("ap-northeast-1");
//        assertTrue(stage3ConfigsAfterFull.stream().anyMatch(c -> c.getVersionId().equals(created4.getVersionId())));
//
//        List<ApiRecordConfigBO> hangzhouConfigs = apiRecordConfigService.getActiveByRegion("cn-hangzhou");
//        assertTrue(hangzhouConfigs.stream().anyMatch(c -> c.getVersionId().equals(created4.getVersionId())));
//    }
//
//    @Test
//    void testGetActiveByIdentifierAndRegion() {
//        // 1. 创建并发布配置到阶段1
//        ApiRecordConfigBO config = createTestConfig("test-api");
//        ApiRecordConfigBO created = apiRecordConfigService.create(config);
//        publishService.publishByStage(created.getVersionId(), "API_RECORD", GrayStage.STAGE_1, "test-user");
//
//        // 2. 验证阶段1地域(马来西亚)可以看到配置
//        ApiRecordConfigBO stage1Config = apiRecordConfigService.getActiveByIdentifierAndRegion(
//            created.getIdentifier(),
//            "ap-southeast-3"
//        );
//        assertNotNull(stage1Config);
//        assertEquals(created.getVersionId(), stage1Config.getVersionId());
//
//        // 3. 验证阶段2地域(成都)看不到阶段1的配置
//        ApiRecordConfigBO stage2Config = apiRecordConfigService.getActiveByIdentifierAndRegion(
//            created.getIdentifier(),
//            "cn-chengdu"
//        );
//        assertNull(stage2Config);
//
//        // 4. 验证核心地域(杭州)可以看到配置
//        ApiRecordConfigBO hangzhouConfig = apiRecordConfigService.getActiveByIdentifierAndRegion(
//            created.getIdentifier(),
//            "cn-hangzhou"
//        );
//        assertNotNull(hangzhouConfig);
//        assertEquals(created.getVersionId(), hangzhouConfig.getVersionId());
//    }
//
//    @Test
//    void testGetConfigDiff() {
//        // 1. 创建并发布第一个配置到阶段1
//        ApiRecordConfigBO config1 = createTestConfig("test-api-1");
//        ApiRecordConfigBO created1 = apiRecordConfigService.create(config1);
//        publishService.publishByStage(created1.getVersionId(), "API_RECORD", GrayStage.STAGE_1, "test-user");
//
//        // 2. 创建并发布第二个配置到阶段2
//        ApiRecordConfigBO config2 = createTestConfig("test-api-2");
//        ApiRecordConfigBO created2 = apiRecordConfigService.create(config2);
//        publishService.publishByStage(created2.getVersionId(), "API_RECORD", GrayStage.STAGE_2, "test-user");
//
//        // 3. 创建并发布第三个配置到阶段3
//        ApiRecordConfigBO config3 = createTestConfig("test-api-3");
//        ApiRecordConfigBO created3 = apiRecordConfigService.create(config3);
//        publishService.publishByStage(created3.getVersionId(), "API_RECORD", GrayStage.STAGE_3, "test-user");
//
//        // 4. 验证阶段1地域(马来西亚)的配置差异
//        ConfigDiffResponse<ApiRecordConfigBO> stage1Diff = apiRecordConfigService.getConfigDiff(
//            Arrays.asList(created1.getVersionId()),
//            "ap-southeast-3"
//        );
//        assertEquals(1, stage1Diff.getUpdatedConfigs().size());
//        assertEquals(1, stage1Diff.getActiveVersionIds().size());
//        assertTrue(stage1Diff.getActiveVersionIds().contains(created1.getVersionId()));
//        assertTrue(stage1Diff.getDeprecatedVersionIds().isEmpty());
//
//        // 5. 验证阶段2地域(成都)的配置差异
//        ConfigDiffResponse<ApiRecordConfigBO> stage2Diff = apiRecordConfigService.getConfigDiff(
//            Arrays.asList(created2.getVersionId()),
//            "cn-chengdu"
//        );
//        assertEquals(1, stage2Diff.getUpdatedConfigs().size());
//        assertEquals(1, stage2Diff.getActiveVersionIds().size());
//        assertTrue(stage2Diff.getActiveVersionIds().contains(created2.getVersionId()));
//        assertTrue(stage2Diff.getDeprecatedVersionIds().isEmpty());
//
//        // 6. 验证阶段3地域(日本)的配置差异
//        ConfigDiffResponse<ApiRecordConfigBO> stage3Diff = apiRecordConfigService.getConfigDiff(
//            Arrays.asList(created3.getVersionId()),
//            "ap-northeast-1"
//        );
//        assertEquals(1, stage3Diff.getUpdatedConfigs().size());
//        assertEquals(1, stage3Diff.getActiveVersionIds().size());
//        assertTrue(stage3Diff.getActiveVersionIds().contains(created3.getVersionId()));
//        assertTrue(stage3Diff.getDeprecatedVersionIds().isEmpty());
//
//        // 7. 验证核心地域(杭州)的配置差异
//        ConfigDiffResponse<ApiRecordConfigBO> hangzhouDiff = apiRecordConfigService.getConfigDiff(
//            Arrays.asList(created1.getVersionId(), created2.getVersionId(), created3.getVersionId()),
//            "cn-hangzhou"
//        );
//        assertEquals(3, hangzhouDiff.getUpdatedConfigs().size());
//        assertEquals(3, hangzhouDiff.getActiveVersionIds().size());
//        assertTrue(hangzhouDiff.getActiveVersionIds().containsAll(Arrays.asList(
//            created1.getVersionId(),
//            created2.getVersionId(),
//            created3.getVersionId()
//        )));
//        assertTrue(hangzhouDiff.getDeprecatedVersionIds().isEmpty());
//
//        // 8. 验证使用不存在的版本ID
//        ConfigDiffResponse<ApiRecordConfigBO> nonExistentDiff = apiRecordConfigService.getConfigDiff(
//            Arrays.asList("non-existent-version"),
//            "cn-hangzhou"
//        );
//        assertEquals(3, nonExistentDiff.getUpdatedConfigs().size());
//        assertEquals(3, nonExistentDiff.getActiveVersionIds().size());
//        assertEquals(1, nonExistentDiff.getDeprecatedVersionIds().size());
//        assertTrue(nonExistentDiff.getDeprecatedVersionIds().contains("non-existent-version"));
//    }
    
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