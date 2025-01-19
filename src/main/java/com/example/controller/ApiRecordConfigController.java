package com.example.controller;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.ApiRecordConfig;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apirecord")
@Tag(name = "API记录配置", description = "API记录配置相关接口")
public class ApiRecordConfigController {
    
    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    /**
     * 创建API记录配置
     */
    @PostMapping
    @Operation(summary = "创建API记录配置")
    public ApiRecordConfig create(@RequestBody ApiRecordConfig config) {
        return apiRecordConfigService.create(config);
    }

    /**
     * 更新API记录配置
     */
    @PutMapping("/{versionId}")
    @Operation(summary = "更新API记录配置")
    public ApiRecordConfig update(
        @PathVariable String versionId,
        @RequestBody ApiRecordConfig config
    ) {
        return apiRecordConfigService.update(versionId, config);
    }

    /**
     * 获取指定版本的配置
     */
    @GetMapping("/{versionId}")
    @Operation(summary = "获取指定版本的配置")
    public ApiRecordConfig getByVersionId(@PathVariable String versionId) {
        return apiRecordConfigService.findByVersionId(versionId);
    }

    /**
     * 获取所有已发布的配置
     */
    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的配置")
    public List<ApiRecordConfig> getAllPublished() {
        return apiRecordConfigService.getAllPublished();
    }

    /**
     * 获取指定API的所有已发布配置
     */
    @GetMapping("/published/{identifier}")
    @Operation(summary = "获取指定API的所有已发布配置")
    public List<ApiRecordConfig> getPublishedByIdentifier(@PathVariable String identifier) {
        return apiRecordConfigService.getPublishedByIdentifier(identifier);
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    @GetMapping("/active/{identifier}")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ApiRecordConfig getActiveByIdentifier(
        @PathVariable String identifier,
        @RequestParam String region
    ) {
        return apiRecordConfigService.getActiveByIdentifierAndRegion(identifier, region);
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @GetMapping("/active")
    @Operation(summary = "获取指定地域生效的所有配置")
    public List<ApiRecordConfig> getActiveByRegion(@RequestParam String region) {
        return apiRecordConfigService.getActiveByRegion(region);
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ConfigDiffResponse<ApiRecordConfig> getConfigDiff(@RequestBody ConfigDiffRequest request) {
        return apiRecordConfigService.getConfigDiff(request);
    }
} 