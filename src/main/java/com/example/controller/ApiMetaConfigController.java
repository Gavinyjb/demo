package com.example.controller;

import com.example.model.ApiMetaConfig;
import com.example.service.ApiMetaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-meta")
@Tag(name = "API Meta配置", description = "API Meta配置相关接口")
public class ApiMetaConfigController {
    
    @Autowired
    private ApiMetaConfigService apiMetaConfigService;

    @PostMapping
    @Operation(summary = "创建API Meta配置")
    public ResponseEntity<ApiMetaConfig> create(@RequestBody ApiMetaConfig config) {
        return ResponseEntity.ok(apiMetaConfigService.create(config));
    }

    @PutMapping("/{versionId}")
    @Operation(summary = "更新API Meta配置")
    public ResponseEntity<ApiMetaConfig> update(
            @PathVariable String versionId,
            @RequestBody ApiMetaConfig config) {
        return ResponseEntity.ok(apiMetaConfigService.update(versionId, config));
    }

    @GetMapping("/published/all")
    @Operation(summary = "获取所有已发布的API Meta配置")
    public ResponseEntity<List<ApiMetaConfig>> getAllPublished() {
        return ResponseEntity.ok(apiMetaConfigService.getAllPublished());
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "获取指定地域的API Meta配置")
    public ResponseEntity<List<ApiMetaConfig>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(apiMetaConfigService.getActiveByRegion(region));
    }

    @GetMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiMetaConfig> getActiveConfig(
            @RequestParam String identifier,
            @RequestParam String region) {
        return ResponseEntity.ok(
            apiMetaConfigService.getActiveByIdentifierAndRegion(identifier, region));
    }

    @GetMapping("/published")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiMetaConfig>> getPublishedConfigs(
            @RequestParam String identifier) {
        return ResponseEntity.ok(
            apiMetaConfigService.getPublishedByIdentifier(identifier));
    }

    @PostMapping("/{versionId}/publish")
    @Operation(summary = "发布配置")
    public ResponseEntity<Void> publish(
            @PathVariable String versionId,
            @RequestParam String stage,
            @RequestParam String operator) {
        apiMetaConfigService.publish(versionId, stage, operator);
        return ResponseEntity.ok().build();
    }
} 