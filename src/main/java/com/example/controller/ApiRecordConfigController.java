package com.example.controller;

import com.example.model.bo.ApiRecordConfigBO;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/apirecord")
@Tag(name = "API记录配置", description = "API记录配置相关接口")
public class ApiRecordConfigController {

    @Autowired
    private ApiRecordConfigService apiRecordConfigService;

    @Data
    public static class CreateRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String basicConfig;
        private String eventConfig;
        private String userIdentityConfig;
        private String requestConfig;
        private String responseConfig;
        private String filterConfig;
        private String referenceResourceConfig;
    }

    @Data
    public static class UpdateRequest {
        private String versionId;
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String basicConfig;
        private String eventConfig;
        private String userIdentityConfig;
        private String requestConfig;
        private String responseConfig;
        private String filterConfig;
        private String referenceResourceConfig;
    }

    @Data
    public static class GetByApiRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
    }

    @Data 
    public static class GetActiveRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String region;
    }

    @Data
    public static class DiffRequest {
        private List<String> versionIds;
        private String region;
    }

    /**
     * 创建API记录配置
     */
    @PostMapping("/create")
    @Operation(summary = "创建API记录配置")
    public ResponseEntity<ApiRecordConfigBO> create(@RequestBody CreateRequest request) {
        ApiRecordConfigBO config = new ApiRecordConfigBO();
        config.setGatewayType(request.getGatewayType());
        config.setGatewayCode(request.getGatewayCode());
        config.setApiVersion(request.getApiVersion());
        config.setApiName(request.getApiName());
        config.setBasicConfig(request.getBasicConfig());
        config.setEventConfig(request.getEventConfig());
        config.setUserIdentityConfig(request.getUserIdentityConfig());
        config.setRequestConfig(request.getRequestConfig());
        config.setResponseConfig(request.getResponseConfig());
        config.setFilterConfig(request.getFilterConfig());
        config.setReferenceResourceConfig(request.getReferenceResourceConfig());
        
        return ResponseEntity.ok(apiRecordConfigService.create(config));
    }

    /**
     * 更新API记录配置
     */
    @PostMapping("/update")
    @Operation(summary = "更新API记录配置")
    public ResponseEntity<ApiRecordConfigBO> update(@RequestBody UpdateRequest request) {
        ApiRecordConfigBO config = new ApiRecordConfigBO();
        config.setGatewayType(request.getGatewayType());
        config.setGatewayCode(request.getGatewayCode());
        config.setApiVersion(request.getApiVersion());
        config.setApiName(request.getApiName());
        config.setBasicConfig(request.getBasicConfig());
        config.setEventConfig(request.getEventConfig());
        config.setUserIdentityConfig(request.getUserIdentityConfig());
        config.setRequestConfig(request.getRequestConfig());
        config.setResponseConfig(request.getResponseConfig());
        config.setFilterConfig(request.getFilterConfig());
        config.setReferenceResourceConfig(request.getReferenceResourceConfig());
        
        return ResponseEntity.ok(apiRecordConfigService.update(request.getVersionId(), config));
    }

    /**
     * 获取指定版本的配置
     */
    @GetMapping("/get")
    @Operation(summary = "获取指定版本的配置")
    public ResponseEntity<ApiRecordConfigBO> getConfig(@RequestParam String versionId) {
        return ResponseEntity.ok(apiRecordConfigService.findByVersionId(versionId));
    }

    /**
     * 获取所有已发布的配置
     */
    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的配置")
    public ResponseEntity<List<ApiRecordConfigBO>> getAllPublished() {
        return ResponseEntity.ok(apiRecordConfigService.getAllPublished());
    }

    /**
     * 获取指定API的所有已发布配置
     */
    @PostMapping("/published/by-api")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiRecordConfigBO>> getPublishedByApi(@RequestBody GetByApiRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(apiRecordConfigService.getPublishedByIdentifier(identifier));
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    @PostMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiRecordConfigBO> getActiveConfig(@RequestBody GetActiveRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(
            apiRecordConfigService.getActiveByIdentifierAndRegion(identifier, request.getRegion())
        );
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @GetMapping("/active/by-region")
    @Operation(summary = "获取指定地域生效的所有配置")
    public ResponseEntity<List<ApiRecordConfigBO>> getActiveByRegion(@RequestParam String region) {
        return ResponseEntity.ok(apiRecordConfigService.getActiveByRegion(region));
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息")
    public ResponseEntity<List<String>> getConfigDiff(@RequestBody DiffRequest request) {
        return ResponseEntity.ok(apiRecordConfigService.getVersionDiff(request.getVersionIds(), request.getRegion()));
    }
}