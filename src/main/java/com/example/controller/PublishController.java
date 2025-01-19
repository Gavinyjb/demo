package com.example.controller;

import com.example.dto.PublishStageRequest;
import com.example.enums.GrayStage;
import com.example.model.PublishHistory;
import com.example.service.PublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/publish")
@Tag(name = "配置发布", description = "配置发布相关接口")
public class PublishController {
    
    @Autowired
    private PublishService publishService;

    /**
     * 发布配置
     */
    @PostMapping("/{versionId}")
    @Operation(summary = "发布配置")
    public void publish(
        @PathVariable String versionId,
        @RequestParam String configType,
        @RequestParam String stage,
        @RequestParam String operator
    ) {
        publishService.publishConfig(versionId, configType, stage, operator);
    }

    /**
     * 废弃配置
     */
    @PostMapping("/{versionId}/deprecate")
    @Operation(summary = "废弃配置")
    public void deprecate(
        @PathVariable String versionId,
        @RequestParam String configType,
        @RequestParam String operator
    ) {
        publishService.deprecateConfig(versionId, configType, operator);
    }

    /**
     * 回滚到上一个版本
     */
    @PostMapping("/rollback/previous")
    @Operation(summary = "回滚到上一个版本")
    public void rollbackToPrevious(
        @RequestParam String identifier,
        @RequestParam String configType,
        @RequestParam String operator
    ) {
        publishService.rollbackToPrevious(identifier, configType, operator);
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/rollback/{targetVersionId}")
    @Operation(summary = "回滚到指定版本")
    public void rollbackToVersion(
        @RequestParam String identifier,
        @PathVariable String targetVersionId,
        @RequestParam String configType,
        @RequestParam String operator
    ) {
        publishService.rollbackToVersion(identifier, targetVersionId, configType, operator);
    }

    /**
     * 获取发布历史
     */
    @GetMapping("/history/{versionId}")
    @Operation(summary = "获取发布历史")
    public List<PublishHistory> getPublishHistory(@PathVariable String versionId) {
        return publishService.getPublishHistory(versionId);
    }

    /**
     * 获取指定配置类型和灰度阶段的发布历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取指定配置类型和灰度阶段的发布历史")
    public List<PublishHistory> getHistoryByTypeAndStage(
        @RequestParam String configType,
        @RequestParam String stage
    ) {
        return publishService.getHistoryByTypeAndStage(configType, stage);
    }

    @PostMapping("/stage")
    @Operation(summary = "按阶段发布配置")
    public ResponseEntity<Void> publishByStage(@RequestBody PublishStageRequest request) {
        publishService.publishByStage(
            request.getVersionId(),
            request.getConfigType(), GrayStage.valueOf(request.getStage()),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stages")
    @Operation(summary = "获取所有灰度阶段信息")
    public ResponseEntity<Map<String, List<String>>> getGrayStages() {
        Map<String, List<String>> stages = new HashMap<>();
        for (GrayStage stage : GrayStage.values()) {
            stages.put(stage.name(), stage.getRegions());
        }
        return ResponseEntity.ok(stages);
    }
} 