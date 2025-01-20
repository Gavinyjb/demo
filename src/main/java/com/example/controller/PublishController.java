package com.example.controller;

import com.example.dto.PublishStageRequest;
import com.example.dto.RollbackRequest;
import com.example.dto.GetPublishHistoryRequest;
import com.example.enums.GrayStage;
import com.example.model.PublishHistory;
import com.example.service.PublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/publish")
@Tag(name = "配置发布", description = "配置发布相关接口")
public class PublishController {
    
    @Autowired
    private PublishService publishService;

    /**
     * 按阶段发布配置
     */
    @PostMapping("/stage")
    @Operation(summary = "按阶段发布配置")
    public ResponseEntity<Void> publishByStage(@RequestBody PublishStageRequest request) {
        log.info("Publishing config by stage: {}", request);

        publishService.publishByStage(
            request.getVersionId(),
            request.getConfigType(),
            GrayStage.valueOf(request.getStage()),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 废弃配置
     */
    @PostMapping("/deprecate")
    @Operation(summary = "废弃配置")
    public ResponseEntity<Void> deprecate(@RequestBody PublishStageRequest request) {
        log.info("Deprecating config: {}", request);
        publishService.deprecateConfig(
            request.getVersionId(),
            request.getConfigType(),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 终止灰度发布
     */
    @PostMapping("/rollback/gray")
    @Operation(summary = "终止灰度发布", description = "终止当前正在灰度中的配置，使用全量发布的配置")
    public ResponseEntity<Void> rollbackGray(@RequestBody RollbackRequest request) {
        log.info("Rolling back graying config: {}", request);
        publishService.rollbackGrayConfig(
            request.getIdentifier(),
            request.getConfigType(),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 回滚到历史版本
     */
    @PostMapping("/rollback/version")
    @Operation(summary = "回滚到历史版本", description = "废弃当前生效的配置，使用指定的历史版本")
    public ResponseEntity<Void> rollbackVersion(@RequestBody RollbackRequest request) {
        log.info("Rolling back to version: {}", request);
        if (request.getTargetVersionId() == null) {
            throw new IllegalArgumentException("Target version ID is required");
        }
        publishService.rollbackToVersion(
            request.getIdentifier(),
            request.getTargetVersionId(),
            request.getConfigType(),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 获取发布历史
     */
    @PostMapping("/history")
    @Operation(summary = "获取发布历史")
    public ResponseEntity<List<PublishHistory>> getPublishHistory(
            @RequestBody GetPublishHistoryRequest request) {
        log.info("Getting publish history: {}", request);
        
        if (request.getVersionId() != null) {
            return ResponseEntity.ok(publishService.getPublishHistory(request.getVersionId()));
        } else if (request.getConfigType() != null && request.getStage() != null) {
            return ResponseEntity.ok(publishService.getHistoryByTypeAndStage(
                request.getConfigType(), 
                request.getStage()
            ));
        } else {
            throw new IllegalArgumentException("Must provide either versionId or (configType and stage)");
        }
    }

    /**
     * 获取所有灰度阶段信息
     */
    @PostMapping("/stages")
    @Operation(summary = "获取所有灰度阶段信息")
    public ResponseEntity<Map<String, List<String>>> getGrayStages() {
        Map<String, List<String>> stages = new HashMap<>();
        for (GrayStage stage : GrayStage.values()) {
            stages.put(stage.name(), stage.getRegions());
        }
        return ResponseEntity.ok(stages);
    }
} 