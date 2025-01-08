package com.example.controller;

import com.example.model.ApiRecordConfig;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-record")
@Tag(name = "API记录配置", description = "API记录配置相关接口")
public class ApiRecordConfigController {
    
    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @PostMapping
    @Operation(summary = "创建API记录配置")
    public ResponseEntity<ApiRecordConfig> create(@RequestBody ApiRecordConfig config) {
        return ResponseEntity.ok(apiRecordConfigService.create(config));
    }

    @PutMapping("/{versionId}")
    @Operation(summary = "更新API记录配置")
    public ResponseEntity<ApiRecordConfig> update(
            @PathVariable String versionId,
            @RequestBody ApiRecordConfig config) {
        return ResponseEntity.ok(apiRecordConfigService.update(versionId, config));
    }

    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的API记录配置")
    public ResponseEntity<List<ApiRecordConfig>> getAllPublished() {
        return ResponseEntity.ok(apiRecordConfigService.getAllPublished());
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "获取指定地域的API记录配置")
    public ResponseEntity<List<ApiRecordConfig>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(apiRecordConfigService.getActiveByRegion(region));
    }

    @GetMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiRecordConfig> getActiveConfig(
            @RequestParam String gatewayType,
            @RequestParam String gatewayCode,
            @RequestParam String apiVersion,
            @RequestParam String apiName,
            @RequestParam String region) {
        return ResponseEntity.ok(
            apiRecordConfigService.getActiveByIdentifierAndRegion(
                gatewayType, gatewayCode, apiVersion, apiName, region));
    }

    @GetMapping("/published")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiRecordConfig>> getPublishedConfigs(
            @RequestParam String gatewayType,
            @RequestParam String gatewayCode,
            @RequestParam String apiVersion,
            @RequestParam String apiName) {
        return ResponseEntity.ok(
            apiRecordConfigService.getPublishedByIdentifier(
                gatewayType, gatewayCode, apiVersion, apiName));
    }
} 