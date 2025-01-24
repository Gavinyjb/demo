package com.example.controller;

import com.example.enums.ConfigStatus;
import com.example.enums.ConfigType;
import com.example.enums.GrayStage;
import com.example.model.PublishHistory;
import com.example.model.response.OpenApiResponse;
import com.example.service.PublishService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class PublishControllerTest {

    @Autowired
    private PublishController publishController;

    @Autowired
    private PublishService publishService;

    @Test
    void testPublishLifecycle() {
        // 1. 创建发布请求
        PublishController.PublishRequest publishRequest = new PublishController.PublishRequest();
        publishRequest.setVersionId("TEST-001");
        publishRequest.setConfigType(ConfigType.DATA_SOURCE.name());
        publishRequest.setStage(GrayStage.STAGE_1.name());
        publishRequest.setOperator("test-user");

        // 2. 执行灰度发布
        OpenApiResponse<Boolean> response = publishController.publishByStage(publishRequest);
        assertTrue(response.isSuccess());
        assertNotNull(response.getRequestId());
        assertTrue(response.getData());

        // 3. 查询发布历史
        PublishController.HistoryRequest historyRequest = new PublishController.HistoryRequest();
        historyRequest.setVersionId("TEST-001");
        
        OpenApiResponse<List<PublishHistory>> historyResponse = publishController.getPublishHistory(historyRequest);
        assertTrue(historyResponse.isSuccess());
        assertNotNull(historyResponse.getRequestId());
        
        List<PublishHistory> history = historyResponse.getData();
        assertFalse(history.isEmpty());
        assertEquals("TEST-001", history.get(0).getVersionId());
        assertEquals(GrayStage.STAGE_1.name(), history.get(0).getStage());
        assertEquals("test-user", history.get(0).getOperator());

        // 4. 废弃配置
        OpenApiResponse<Boolean> deprecateResponse = publishController.deprecate(publishRequest);
        assertTrue(deprecateResponse.isSuccess());
        assertTrue(deprecateResponse.getData());

        // 5. 再次查询历史验证状态
        historyResponse = publishController.getPublishHistory(historyRequest);
        history = historyResponse.getData();
        assertEquals(ConfigStatus.DEPRECATED.name(), history.get(0).getConfigStatus());
    }

    @Test
    void testRollbackFlow() {
        // 1. 准备回滚请求
        PublishController.RollbackRequest rollbackRequest = new PublishController.RollbackRequest();
        rollbackRequest.setIdentifier("test:identifier");
        rollbackRequest.setConfigType(ConfigType.DATA_SOURCE.name());
        rollbackRequest.setOperator("test-user");

        // 2. 测试灰度回滚
        OpenApiResponse<Boolean> response = publishController.rollbackGray(rollbackRequest);
        assertTrue(response.isSuccess());
        assertTrue(response.getData());

        // 3. 测试版本回滚
        rollbackRequest.setTargetVersionId("TEST-002");
        OpenApiResponse<Boolean> versionResponse = publishController.rollbackVersion(rollbackRequest);
        assertTrue(versionResponse.isSuccess());
        assertTrue(versionResponse.getData());
    }

    @Test
    void testGetGrayStages() {
        OpenApiResponse<Map<String, List<String>>> response = publishController.getGrayStages();
        assertTrue(response.isSuccess());
        assertNotNull(response.getRequestId());
        
        Map<String, List<String>> stages = response.getData();
        assertNotNull(stages);
        assertFalse(stages.isEmpty());
        
        // 验证所有灰度阶段都存在
        assertTrue(stages.containsKey(GrayStage.STAGE_1.name()));
        assertTrue(stages.containsKey(GrayStage.STAGE_2.name()));
        assertTrue(stages.containsKey(GrayStage.FULL.name()));
        
        // 验证地域信息
        assertTrue(stages.get(GrayStage.STAGE_1.name()).contains("ap-southeast-2"));
        assertTrue(stages.get(GrayStage.STAGE_2.name()).contains("cn-chengdu"));
        assertTrue(stages.get(GrayStage.FULL.name()).contains("cn-hangzhou"));
    }

    @Test
    void testHistoryQueryByTypeAndStage() {
        // 1. 准备历史查询请求
        PublishController.HistoryRequest request = new PublishController.HistoryRequest();
        request.setConfigType(ConfigType.DATA_SOURCE.name());
        request.setStage(GrayStage.STAGE_1.name());

        // 2. 执行查询
        OpenApiResponse<List<PublishHistory>> response = publishController.getPublishHistory(request);
        assertTrue(response.isSuccess());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getData());
    }

    @Test
    void testInvalidRequests() {
        // 1. 测试缺少必填参数的发布请求
        PublishController.PublishRequest invalidPublishRequest = new PublishController.PublishRequest();
        assertThrows(Exception.class, () -> publishController.publishByStage(invalidPublishRequest));

        // 2. 测试版本回滚时缺少目标版本
        PublishController.RollbackRequest invalidRollbackRequest = new PublishController.RollbackRequest();
        invalidRollbackRequest.setIdentifier("test:identifier");
        invalidRollbackRequest.setConfigType(ConfigType.DATA_SOURCE.name());
        invalidRollbackRequest.setOperator("test-user");
        assertThrows(IllegalArgumentException.class, 
            () -> publishController.rollbackVersion(invalidRollbackRequest));

        // 3. 测试历史查询参数无效
        PublishController.HistoryRequest invalidHistoryRequest = new PublishController.HistoryRequest();
        assertThrows(IllegalArgumentException.class, 
            () -> publishController.getPublishHistory(invalidHistoryRequest));
    }
} 