package com.example.service;

import com.example.model.DataSourceConfig;
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
        
        // 验证旧版本被废弃
        DataSourceConfigBO oldConfig = dataSourceConfigService
            .findByVersionId(created.getVersionId());
        assertEquals(ConfigStatus.DEPRECATED.name(), oldConfig.getConfigStatus());
        
        // 5. 回滚灰度发布
        publishService.rollbackGrayConfig(
            "test-source",
            "DATA_SOURCE",
            "test-user"
        );
        
        // 验证新版本被废弃，旧版本重新生效
        DataSourceConfigBO rolledBack = dataSourceConfigService
            .getActiveByIdentifierAndRegion("test-source", "ap-southeast-2");
        assertEquals(created.getVersionId(), rolledBack.getVersionId());
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