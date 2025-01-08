package com.example.controller;

import com.example.model.DataSourceConfig;
import com.example.model.ApiRecordConfig;
import com.example.model.PublishHistory;
import com.example.service.ConfigService;
import com.example.util.RegionProvider;
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
@Tag(name = "配置管理", description = "配置管理相关接口")
public class ConfigController {
    @Autowired
    private ConfigService configService;

    @Autowired
    private RegionProvider regionProvider;

    @PostMapping("/datasource")
    @Operation(summary = "创建数据源配置")
    public ResponseEntity<DataSourceConfig> createDataSource(@RequestBody DataSourceConfig config) {
        return ResponseEntity.ok(configService.createDataSource(config));
    }

    @PutMapping("/datasource/{versionId}")
    @Operation(summary = "更新数据源配置")
    public ResponseEntity<DataSourceConfig> updateDataSource(
            @PathVariable String versionId,
            @RequestBody DataSourceConfig config) {
        return ResponseEntity.ok(configService.updateDataSource(versionId, config));
    }

    @PostMapping("/api-record")
    @Operation(summary = "创建API记录配置")
    public ResponseEntity<ApiRecordConfig> createApiRecord(@RequestBody ApiRecordConfig config) {
        return ResponseEntity.ok(configService.createApiRecord(config));
    }

    @PutMapping("/api-record/{versionId}")
    @Operation(summary = "更新API记录配置")
    public ResponseEntity<ApiRecordConfig> updateApiRecord(
            @PathVariable String versionId,
            @RequestBody ApiRecordConfig config) {
        return ResponseEntity.ok(configService.updateApiRecord(versionId, config));
    }

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

    @PostMapping("/rollback")
    @Operation(summary = "回滚配置")
    public ResponseEntity<Void> rollbackConfig(
            @RequestParam String currentVersionId,
            @RequestParam String targetVersionId,
            @RequestParam String operator,
            @RequestBody List<String> grayGroups) {
        configService.rollbackConfig(currentVersionId, targetVersionId, grayGroups, operator);
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

    @GetMapping("/history/{versionId}")
    @Operation(summary = "获取发布历史")
    public ResponseEntity<List<PublishHistory>> getPublishHistory(@PathVariable String versionId) {
        return ResponseEntity.ok(configService.getPublishHistory(versionId));
    }

    @GetMapping("/datasource/current-region")
    @Operation(summary = "获取当前地域的数据源配置")
    public ResponseEntity<Map<String, Object>> getCurrentRegionDataSources() {
        String currentRegion = regionProvider.getCurrentRegion();
        List<DataSourceConfig> configs = configService.getActiveDataSourceConfigsByRegion(currentRegion);
        
        Map<String, Object> response = new HashMap<>();
        response.put("region", currentRegion);
        response.put("configs", configs);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/datasource/region/{region}")
    @Operation(summary = "获取指定地域的数据源配置")
    public ResponseEntity<List<DataSourceConfig>> getDataSourcesByRegion(@PathVariable String region) {
        return ResponseEntity.ok(configService.getActiveDataSourceConfigsByRegion(region));
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

    @GetMapping("/stages")
    @Operation(summary = "获取所有灰度阶段信息")
    public ResponseEntity<Map<String, List<String>>> getGrayStages() {
        Map<String, List<String>> stages = new HashMap<>();
        for (GrayStage stage : GrayStage.values()) {
            stages.put(stage.name(), stage.getRegions());
        }
        return ResponseEntity.ok(stages);
    }

    @GetMapping("/datasource/{source}/active")
    @Operation(summary = "获取指定source在当前地域生效的配置")
    public ResponseEntity<DataSourceConfig> getActiveDataSourceConfig(
            @PathVariable String source) {
        String currentRegion = regionProvider.getCurrentRegion();
        return ResponseEntity.ok(
            configService.getActiveDataSourceConfig(source, currentRegion));
    }

    @GetMapping("/datasource/{source}/published")
    @Operation(summary = "获取指定source的所有已发布配置")
    public ResponseEntity<List<DataSourceConfig>> getPublishedDataSourceConfigs(
            @PathVariable String source) {
        return ResponseEntity.ok(configService.getPublishedDataSourceConfigs(source));
    }
} 