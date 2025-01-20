package com.example.controller;

import com.example.model.bo.DataSourceConfigBO;
import com.example.service.DataSourceConfigService;
import com.example.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datasource/configs")
@Tag(name = "数据源配置", description = "数据源配置相关接口")
public class DataSourceConfigController {
    
    @Autowired
    private DataSourceConfigService dataSourceConfigService;

    /**
     * 创建数据源配置
     */
    @PostMapping("/create")
    @Operation(summary = "创建数据源配置")
    public ResponseEntity<DataSourceConfigBO> create(@RequestBody DataSourceConfigBO config) {
        return ResponseEntity.ok(dataSourceConfigService.create(config));
    }

    /**
     * 更新数据源配置
     */
    @PostMapping("/update")
    @Operation(summary = "更新数据源配置")
    public ResponseEntity<DataSourceConfigBO> update(@RequestBody UpdateConfigRequest request) {
        return ResponseEntity.ok(
            dataSourceConfigService.update(request.getVersionId(), request.getConfig())
        );
    }

    /**
     * 获取指定版本的配置
     */
    @PostMapping("/get")
    @Operation(summary = "获取指定版本的配置")
    public ResponseEntity<DataSourceConfigBO> getConfig(@RequestBody GetConfigRequest request) {
        return ResponseEntity.ok(dataSourceConfigService.findByVersionId(request.getVersionId()));
    }

    /**
     * 获取所有已发布的配置
     */
    @PostMapping("/published")
    @Operation(summary = "获取所有已发布的配置")
    public ResponseEntity<List<DataSourceConfigBO>> getAllPublished() {
        return ResponseEntity.ok(dataSourceConfigService.getAllPublished());
    }

    /**
     * 获取指定数据源的所有已发布配置
     */
    @PostMapping("/published/by-source")
    @Operation(summary = "获取指定数据源的所有已发布配置")
    public ResponseEntity<List<DataSourceConfigBO>> getPublishedBySource(@RequestBody GetConfigRequest request) {
        return ResponseEntity.ok(dataSourceConfigService.getPublishedByIdentifier(request.getName()));
    }

    /**
     * 获取指定数据源在指定地域生效的配置
     */
    @PostMapping("/active")
    @Operation(summary = "获取指定数据源在指定地域生效的配置")
    public ResponseEntity<DataSourceConfigBO> getActiveConfig(@RequestBody GetConfigRequest request) {
        return ResponseEntity.ok(
            dataSourceConfigService.getActiveByIdentifierAndRegion(request.getName(), request.getRegion())
        );
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @PostMapping("/active/by-region")
    @Operation(summary = "获取指定地域生效的所有配置")
    public ResponseEntity<List<DataSourceConfigBO>> getActiveByRegion(@RequestBody GetConfigRequest request) {
        return ResponseEntity.ok(dataSourceConfigService.getActiveByRegion(request.getRegion()));
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ResponseEntity<ConfigDiffResponse<DataSourceConfigBO>> getConfigDiff(
            @RequestBody ConfigDiffRequest request) {
        return ResponseEntity.ok(dataSourceConfigService.getConfigDiff(request));
    }
} 