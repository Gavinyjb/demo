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

    @PostMapping
    @Operation(summary = "发布配置")
    public ResponseEntity<Void> publish(
            @RequestParam String versionId,
            @RequestParam String configType,
            @RequestParam String operator,
            @RequestBody List<String> grayGroups) {
        publishService.publish(versionId, configType, grayGroups, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stage")
    @Operation(summary = "按阶段发布配置")
    public ResponseEntity<Void> publishByStage(@RequestBody PublishStageRequest request) {
        publishService.publishByStage(
            request.getVersionId(),
            request.getConfigType(),
            request.getStage(),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback")
    @Operation(summary = "回滚配置")
    public ResponseEntity<Void> rollback(
            @RequestParam String currentVersionId,
            @RequestParam String targetVersionId,
            @RequestParam String operator) {
        publishService.rollback(currentVersionId, targetVersionId, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deprecate")
    @Operation(summary = "废弃配置")
    public ResponseEntity<Void> deprecate(
            @RequestParam String versionId,
            @RequestParam String operator) {
        publishService.deprecate(versionId, operator);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{versionId}")
    @Operation(summary = "获取发布历史")
    public ResponseEntity<List<PublishHistory>> getHistory(@PathVariable String versionId) {
        return ResponseEntity.ok(publishService.getHistory(versionId));
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