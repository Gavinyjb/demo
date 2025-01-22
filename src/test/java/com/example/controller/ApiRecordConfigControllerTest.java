package com.example.controller;

import com.example.enums.ConfigStatus;
import com.example.enums.GrayStage;
import com.example.model.bo.ApiRecordConfigBO;
import com.example.service.ApiRecordConfigService;
import com.example.service.PublishService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

@SpringBootTest
@Transactional
@Slf4j
class ApiRecordConfigControllerTest {

    @Autowired
    private ApiRecordConfigController controller;

    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @Autowired
    private PublishService publishService;

    @Test
    void testDeleteDraftConfig() {
        // 1. 创建一个草稿配置
        ApiRecordConfigController.CreateRequest createRequest = new ApiRecordConfigController.CreateRequest();
        createRequest.setGatewayType("TEST");
        createRequest.setGatewayCode("test-gateway");
        createRequest.setApiVersion("1.0.0");
        createRequest.setApiName("test-api");
        createRequest.setBasicConfig("{}");

        ApiRecordConfigController.ApiRecordResponse created = controller.create(createRequest).getBody();
        assertNotNull(created);
        assertEquals(ConfigStatus.DRAFT.name(), created.getConfigStatus());

        // 2. 按版本ID删除草稿配置
        ApiRecordConfigController.DeleteRequest deleteRequest = new ApiRecordConfigController.DeleteRequest();
        deleteRequest.setVersionId(created.getVersionId());
        log.warn(created.getVersionId());
        
        controller.delete(deleteRequest);

        // 3. 验证配置已被删除
        assertNull(apiRecordConfigService.findByVersionId(created.getVersionId()));
    }

    @Test
    void testDeleteDeprecatedConfig() {
        // 1. 创建配置并发布
        ApiRecordConfigController.CreateRequest createRequest = new ApiRecordConfigController.CreateRequest();
        createRequest.setGatewayType("TEST");
        createRequest.setGatewayCode("test-gateway");
        createRequest.setApiVersion("1.0.0");
        createRequest.setApiName("test-api");
        createRequest.setBasicConfig("{}");

        ApiRecordConfigController.ApiRecordResponse created = controller.create(createRequest).getBody();
        assertNotNull(created);

        // 2. 发布配置
        publishService.publishByStage(created.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 3. 创建新版本并发布，使旧版本废弃
        ApiRecordConfigController.UpdateRequest updateRequest = new ApiRecordConfigController.UpdateRequest();
        updateRequest.setVersionId(created.getVersionId());
        updateRequest.setGatewayType("TEST");
        updateRequest.setGatewayCode("test-gateway");
        updateRequest.setApiVersion("1.0.0");
        updateRequest.setApiName("test-api");
        updateRequest.setBasicConfig("{\"updated\": true}");

        ApiRecordConfigController.ApiRecordResponse updated = controller.update(updateRequest).getBody();
        assertNotNull(updated);
        publishService.publishByStage(updated.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 4. 删除废弃的配置
        ApiRecordConfigController.DeleteRequest deleteRequest = new ApiRecordConfigController.DeleteRequest();
        deleteRequest.setGatewayType("TEST");
        deleteRequest.setGatewayCode("test-gateway");
        deleteRequest.setApiVersion("1.0.0");
        deleteRequest.setApiName("test-api");
        
        controller.delete(deleteRequest);

        // 5. 验证废弃配置已被删除，但新版本仍然存在
        assertNull(apiRecordConfigService.findByVersionId(created.getVersionId()));
        assertNotNull(apiRecordConfigService.findByVersionId(updated.getVersionId()));
    }

    @Test
    void testCannotDeletePublishedConfig() {
        // 1. 创建配置并发布
        ApiRecordConfigController.CreateRequest createRequest = new ApiRecordConfigController.CreateRequest();
        createRequest.setGatewayType("TEST");
        createRequest.setGatewayCode("test-gateway");
        createRequest.setApiVersion("1.0.0");
        createRequest.setApiName("test-api");
        createRequest.setBasicConfig("{}");

        ApiRecordConfigController.ApiRecordResponse created = controller.create(createRequest).getBody();
        assertNotNull(created);

        // 2. 发布配置
        publishService.publishByStage(created.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 3. 尝试删除已发布的配置
        ApiRecordConfigController.DeleteRequest deleteRequest = new ApiRecordConfigController.DeleteRequest();
        deleteRequest.setVersionId(created.getVersionId());

        // 4. 验证无法删除已发布的配置
        assertThrows(RuntimeException.class, () -> controller.delete(deleteRequest));
        assertNotNull(apiRecordConfigService.findByVersionId(created.getVersionId()));
    }

    @Test
    void testGetConfigDiff() {
        // 1. 创建并发布第一个配置
        ApiRecordConfigController.CreateRequest createRequest = new ApiRecordConfigController.CreateRequest();
        createRequest.setGatewayType("TEST");
        createRequest.setGatewayCode("test-gateway");
        createRequest.setApiVersion("1.0.0");
        createRequest.setApiName("test-api");
        createRequest.setBasicConfig("{}");

        ApiRecordConfigController.ApiRecordResponse config1 = controller.create(createRequest).getBody();
        assertNotNull(config1);
        publishService.publishByStage(config1.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 2. 创建并发布第二个配置（不同API）
        createRequest.setApiName("test-api-2");
        ApiRecordConfigController.ApiRecordResponse config2 = controller.create(createRequest).getBody();
        assertNotNull(config2);
        publishService.publishByStage(config2.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 3. 更新第一个配置，使其成为新版本
        ApiRecordConfigController.UpdateRequest updateRequest = new ApiRecordConfigController.UpdateRequest();
        updateRequest.setVersionId(config1.getVersionId());
        updateRequest.setGatewayType("TEST");
        updateRequest.setGatewayCode("test-gateway");
        updateRequest.setApiVersion("1.0.0");
        updateRequest.setApiName("test-api");
        updateRequest.setBasicConfig("{\"updated\": true}");

        ApiRecordConfigController.ApiRecordResponse config3 = controller.update(updateRequest).getBody();
        assertNotNull(config3);
        publishService.publishByStage(config3.getVersionId(), "API_RECORD", GrayStage.FULL, "test-user");

        // 4. 测试配置差异对比 - 客户端没有任何配置
        ApiRecordConfigController.ConfigDiffRequest diffRequest = new ApiRecordConfigController.ConfigDiffRequest();
        diffRequest.setVersionIds(Arrays.asList());
        diffRequest.setRegion("ap-southeast-1");

        ApiRecordConfigController.ConfigDiffResponse diffResponse1 = controller.getConfigDiff(diffRequest).getBody();
        assertNotNull(diffResponse1);
        assertEquals(2, diffResponse1.getUpdatedConfigs().size());  // 应该返回两个活跃配置
        assertEquals(2, diffResponse1.getActiveVersionIds().size());
        assertTrue(diffResponse1.getDeprecatedVersionIds().isEmpty());

        // 5. 测试配置差异对比 - 客户端持有旧版本
        diffRequest.setVersionIds(Arrays.asList(config1.getVersionId()));
        ApiRecordConfigController.ConfigDiffResponse diffResponse2 = controller.getConfigDiff(diffRequest).getBody();
        assertNotNull(diffResponse2);
        assertEquals(2, diffResponse2.getUpdatedConfigs().size());  // 应该返回新版本和另一个API的配置
        assertEquals(2, diffResponse2.getActiveVersionIds().size());
        assertEquals(1, diffResponse2.getDeprecatedVersionIds().size());
        assertTrue(diffResponse2.getDeprecatedVersionIds().contains(config1.getVersionId()));

        // 6. 测试配置差异对比 - 客户端持有最新版本
        diffRequest.setVersionIds(Arrays.asList(config2.getVersionId(), config3.getVersionId()));
        ApiRecordConfigController.ConfigDiffResponse diffResponse3 = controller.getConfigDiff(diffRequest).getBody();
        assertNotNull(diffResponse3);
        assertTrue(diffResponse3.getUpdatedConfigs().isEmpty());  // 不应该有需要更新的配置
        assertEquals(2, diffResponse3.getActiveVersionIds().size());
        assertTrue(diffResponse3.getDeprecatedVersionIds().isEmpty());

        // 7. 测试配置差异对比 - 客户端持有不存在的版本
        diffRequest.setVersionIds(Arrays.asList("non-existent-version"));
        ApiRecordConfigController.ConfigDiffResponse diffResponse4 = controller.getConfigDiff(diffRequest).getBody();
        assertNotNull(diffResponse4);
        assertEquals(2, diffResponse4.getUpdatedConfigs().size());  // 应该返回所有活跃配置
        assertEquals(2, diffResponse4.getActiveVersionIds().size());
        assertEquals(1, diffResponse4.getDeprecatedVersionIds().size());
        assertTrue(diffResponse4.getDeprecatedVersionIds().contains("non-existent-version"));
    }
} 