package com.example.controller;

import com.example.model.DataSourceConfig;
import com.example.service.DataSourceConfigService;
import com.example.util.RegionProvider;
import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/datasource")
@Tag(name = "数据源配置", description = "数据源配置相关接口")
public class DataSourceConfigController {
    
    @Autowired
    private DataSourceConfigService dataSourceConfigService;
    
    @Autowired
    private RegionProvider regionProvider;

    @PostMapping
    @Operation(summary = "创建数据源配置")
    public ResponseEntity<DataSourceConfig> create(@RequestBody DataSourceConfig config) {
        return ResponseEntity.ok(dataSourceConfigService.create(config));
    }

    @PutMapping("/{versionId}")
    @Operation(summary = "更新数据源配置")
    public ResponseEntity<DataSourceConfig> update(
            @PathVariable String versionId,
            @RequestBody DataSourceConfig config) {
        return ResponseEntity.ok(dataSourceConfigService.update(versionId, config));
    }

    @GetMapping("/current-region")
    @Operation(summary = "获取当前地域的数据源配置")
    public ResponseEntity<Map<String, Object>> getCurrentRegionConfigs() {
        String currentRegion = regionProvider.getCurrentRegion();
        List<DataSourceConfig> configs = dataSourceConfigService.getActiveByRegion(currentRegion);
        
        Map<String, Object> response = new HashMap<>();
        response.put("region", currentRegion);
        response.put("configs", configs);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "获取指定地域的数据源配置")
    public ResponseEntity<List<DataSourceConfig>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(dataSourceConfigService.getActiveByRegion(region));
    }

    @GetMapping("/{source}/active")
    @Operation(summary = "获取指定source在当前地域生效的配置")
    public ResponseEntity<DataSourceConfig> getActiveConfig(@PathVariable String source) {
        String currentRegion = regionProvider.getCurrentRegion();
        return ResponseEntity.ok(
            dataSourceConfigService.getActiveBySourceAndRegion(source, currentRegion));
    }

    @GetMapping("/{source}/published")
    @Operation(summary = "获取指定source的所有已发布配置")
    public ResponseEntity<List<DataSourceConfig>> getPublishedConfigs(
            @PathVariable String source) {
        return ResponseEntity.ok(dataSourceConfigService.getPublishedBySource(source));
    }

    @GetMapping("/{source}/region/{region}")
    @Operation(summary = "获取指定source在指定地域的生效配置")
    public ResponseEntity<DataSourceConfig> getBySourceAndRegion(
            @PathVariable String source,
            @PathVariable String region) {
        return ResponseEntity.ok(
            dataSourceConfigService.getActiveBySourceAndRegion(source, region));
    }

    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ResponseEntity<ConfigDiffResponse> getConfigDiff(@RequestBody ConfigDiffRequest request) {
        return ResponseEntity.ok(dataSourceConfigService.getConfigDiff(request));
    }
} 