package com.example.controller;

import com.example.model.PublishHistory;
import com.example.service.ConfigService;
import com.example.enums.GrayStage;
import com.example.dto.PublishStageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/config")
@Tag(name = "配置管理", description = "配置发布相关接口")
public class ConfigController {
    @Autowired
    private ConfigService configService;

    @PostMapping("/publish")
    @Operation(summary = "发布配置")
    public ResponseEntity<Void> publishConfig(
            @RequestParam String versionId,
            @RequestParam String configType,
            @RequestParam String operator,
            @RequestBody List<String> grayGroups) {
        configService.publishConfig(versionId, configType, grayGroups, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/publish/stage")
    @Operation(summary = "按阶段发布配置")
    public ResponseEntity<Void> publishConfigByStage(@RequestBody PublishStageRequest request) {
        configService.publishConfigByStage(
            request.getVersionId(),
            request.getConfigType(),
            request.getStage(),
            request.getOperator()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deprecate")
    @Operation(summary = "废弃配置")
    public ResponseEntity<Void> deprecateConfig(
            @RequestParam String versionId,
            @RequestParam String operator,
            @RequestBody List<String> grayGroups) {
        configService.deprecateConfig(versionId, grayGroups, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback/previous")
    @Operation(summary = "回滚到上一个版本")
    public ResponseEntity<Void> rollbackToPrevious(
            @RequestParam String identifier,
            @RequestParam String configType,
            @RequestParam String operator) {
        configService.rollbackToPrevious(identifier, configType, operator);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback/version")
    @Operation(summary = "回滚到指定版本")
    public ResponseEntity<Void> rollbackToVersion(
            @RequestParam String identifier,
            @RequestParam String targetVersionId,
            @RequestParam String configType,
            @RequestParam String operator) {
        configService.rollbackToVersion(identifier, targetVersionId, configType, operator);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{versionId}")
    @Operation(summary = "获取发布历史")
    public ResponseEntity<List<PublishHistory>> getPublishHistory(@PathVariable String versionId) {
        return ResponseEntity.ok(configService.getPublishHistory(versionId));
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