package com.example.controller;

import com.example.dto.*;
import com.example.model.bo.ApiMetaConfigBO;
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

    /**
     * 创建API Meta配置
     */
    @PostMapping("/create")
    @Operation(summary = "创建API Meta配置")
    public ResponseEntity<ApiMetaConfigBO> create(@RequestBody ApiMetaConfigBO config) {
        return ResponseEntity.ok(apiMetaConfigService.create(config));
    }

    /**
     * 更新API Meta配置
     */
    @PostMapping("/update")
    @Operation(summary = "更新API Meta配置")
    public ResponseEntity<ApiMetaConfigBO> update(@RequestBody UpdateApiMetaRequest request) {
        return ResponseEntity.ok(
            apiMetaConfigService.update(request.getVersionId(), request.getConfig())
        );
    }

    /**
     * 获取所有已发布的API Meta配置
     */
    @PostMapping("/published")
    @Operation(summary = "获取所有已发布的API Meta配置")
    public ResponseEntity<List<ApiMetaConfigBO>> getAllPublished() {
        return ResponseEntity.ok(apiMetaConfigService.getAllPublished());
    }

    /**
     * 获取指定地域的API Meta配置
     */
    @PostMapping("/active/by-region")
    @Operation(summary = "获取指定地域的API Meta配置")
    public ResponseEntity<List<ApiMetaConfigBO>> getByRegion(@RequestBody GetApiMetaRequest request) {
        return ResponseEntity.ok(apiMetaConfigService.getActiveByRegion(request.getRegion()));
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    @PostMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiMetaConfigBO> getActiveConfig(@RequestBody GetApiMetaRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(
            apiMetaConfigService.getActiveByIdentifierAndRegion(identifier, request.getRegion())
        );
    }

    /**
     * 获取指定API的所有已发布配置
     */
    @PostMapping("/published/by-api")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiMetaConfigBO>> getPublishedConfigs(@RequestBody GetApiMetaRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(
            apiMetaConfigService.getPublishedByIdentifier(identifier)
        );
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ResponseEntity<ConfigDiffResponse<ApiMetaConfigBO>> getConfigDiff(
            @RequestBody ConfigDiffRequest request) {
        return ResponseEntity.ok(apiMetaConfigService.getConfigDiff(request));
    }
}