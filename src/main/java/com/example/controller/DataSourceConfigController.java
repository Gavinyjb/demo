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

    /**
     * 创建数据源配置
     */
    @PostMapping
    @Operation(summary = "创建数据源配置")
    public DataSourceConfig create(@RequestBody DataSourceConfig config) {
        return dataSourceConfigService.create(config);
    }

    /**
     * 更新数据源配置
     */
    @PutMapping("/{versionId}")
    @Operation(summary = "更新数据源配置")
    public DataSourceConfig update(
        @PathVariable String versionId,
        @RequestBody DataSourceConfig config
    ) {
        return dataSourceConfigService.update(versionId, config);
    }

    /**
     * 获取指定版本的配置
     */
    @GetMapping("/{versionId}")
    public DataSourceConfig getByVersionId(@PathVariable String versionId) {
        return dataSourceConfigService.findByVersionId(versionId);
    }

    /**
     * 获取所有已发布的配置
     */
    @GetMapping("/published")
    public List<DataSourceConfig> getAllPublished() {
        return dataSourceConfigService.getAllPublished();
    }

    /**
     * 获取指定数据源的所有已发布配置
     */
    @GetMapping("/published/{source}")
    public List<DataSourceConfig> getPublishedBySource(@PathVariable String source) {
        return dataSourceConfigService.getPublishedByIdentifier(source);
    }

    /**
     * 获取指定数据源在指定地域生效的配置
     */
    @GetMapping("/active/{source}")
    public DataSourceConfig getActiveBySource(
        @PathVariable String source,
        @RequestParam String region
    ) {
        return dataSourceConfigService.getActiveByIdentifierAndRegion(source, region);
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @GetMapping("/active")
    public List<DataSourceConfig> getActiveByRegion(@RequestParam String region) {
        return dataSourceConfigService.getActiveByRegion(region);
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ConfigDiffResponse<DataSourceConfig> getConfigDiff(@RequestBody ConfigDiffRequest request) {
        return dataSourceConfigService.getConfigDiff(request);
    }
} 