package com.example.controller;

import com.example.dto.ConfigDiffRequest;
import com.example.dto.ConfigDiffResponse;
import com.example.model.bo.ApiRecordConfigBO;
import com.example.service.ApiRecordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public static class ApiRecordResponse {
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
        private String configStatus;
        private String gmtCreate;
        private String gmtModified;

        public static ApiRecordResponse fromBO(ApiRecordConfigBO bo) {
            if (bo == null) {
                return null;
            }
            ApiRecordResponse response = new ApiRecordResponse();
            BeanUtils.copyProperties(bo, response);
            return response;
        }
    }

    @Data
    public static class DeleteRequest {
        private String gatewayType;
        private String gatewayCode;
        private String apiVersion;
        private String apiName;
        private String versionId;  // 可选，指定要删除的版本
    }

    @Data
    @Builder
    public static class ApiRecordDiffResponse {
        private List<ApiRecordResponse> updatedConfigs;  // 新增或更新的配置
        private List<String> activeVersionIds;           // 当前生效的版本号
        private List<String> deprecatedVersionIds;       // 已失效的版本号
    }

    /**
     * 创建API记录配置
     */
    @PostMapping("/create")
    @Operation(summary = "创建API记录配置")
    public ResponseEntity<ApiRecordResponse> create(@RequestBody CreateRequest request) {
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
        
        return ResponseEntity.ok(ApiRecordResponse.fromBO(apiRecordConfigService.create(config)));
    }

    /**
     * 更新API记录配置
     */
    @PostMapping("/update")
    @Operation(summary = "更新API记录配置")
    public ResponseEntity<ApiRecordResponse> update(@RequestBody UpdateRequest request) {
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
        
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.update(request.getVersionId(), config)));
    }

    /**
     * 获取指定版本的配置
     */
    @GetMapping("/get")
    @Operation(summary = "获取指定版本的配置")
    public ResponseEntity<ApiRecordResponse> getConfig(@RequestParam String versionId) {
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.findByVersionId(versionId)));
    }

    /**
     * 获取所有已发布的配置
     */
    @GetMapping("/published")
    @Operation(summary = "获取所有已发布的配置")
    public ResponseEntity<List<ApiRecordResponse>> getAllPublished() {
        return ResponseEntity.ok(
            apiRecordConfigService.getAllPublished().stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取指定API的所有已发布配置
     */
    @PostMapping("/published/by-api")
    @Operation(summary = "获取指定API的所有已发布配置")
    public ResponseEntity<List<ApiRecordResponse>> getPublishedByApi(@RequestBody GetByApiRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(
            apiRecordConfigService.getPublishedByIdentifier(identifier).stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取指定API在指定地域生效的配置
     */
    @PostMapping("/active")
    @Operation(summary = "获取指定API在指定地域生效的配置")
    public ResponseEntity<ApiRecordResponse> getActiveConfig(@RequestBody GetActiveRequest request) {
        String identifier = String.format("%s:%s:%s:%s",
            request.getGatewayType(),
            request.getGatewayCode(),
            request.getApiVersion(),
            request.getApiName()
        );
        return ResponseEntity.ok(ApiRecordResponse.fromBO(
            apiRecordConfigService.getActiveByIdentifierAndRegion(identifier, request.getRegion())
        ));
    }

    /**
     * 获取指定地域生效的所有配置
     */
    @GetMapping("/active/by-region")
    @Operation(summary = "获取指定地域生效的所有配置")
    public ResponseEntity<List<ApiRecordResponse>> getActiveByRegion(@RequestParam String region) {
        return ResponseEntity.ok(
            apiRecordConfigService.getActiveByRegion(region).stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取配置变更信息
     */
    @PostMapping("/diff")
    @Operation(summary = "获取配置变更信息", description = "比较客户端版本与服务端版本的差异，返回需要更新的配置")
    public ResponseEntity<ApiRecordDiffResponse> getConfigDiff(@RequestBody ConfigDiffRequest request) {
        ConfigDiffResponse<ApiRecordConfigBO> boResponse = apiRecordConfigService.getConfigDiff(
            request.getVersionIds(),
            request.getRegion()
        );
        
        return ResponseEntity.ok(ApiRecordDiffResponse.builder()
            .updatedConfigs(boResponse.getUpdatedConfigs().stream()
                .map(ApiRecordResponse::fromBO)
                .collect(Collectors.toList()))
            .activeVersionIds(boResponse.getActiveVersionIds())
            .deprecatedVersionIds(boResponse.getDeprecatedVersionIds())
            .build());
    }

    /**
     * 删除配置（仅支持删除草稿和废弃状态的配置）
     */
    @PostMapping("/delete")
    @Operation(summary = "删除配置（仅支持删除草稿和废弃状态的配置）")
    public ResponseEntity<Void> delete(@RequestBody DeleteRequest request) {
        if (request.getVersionId() != null) {
            apiRecordConfigService.deleteByVersionIdWithStatusCheck(request.getVersionId());
        } else {
            String identifier = String.format("%s:%s:%s:%s",
                request.getGatewayType(),
                request.getGatewayCode(),
                request.getApiVersion(),
                request.getApiName()
            );
            apiRecordConfigService.deleteByIdentifierWithStatusCheck(identifier);
        }
        return ResponseEntity.ok().build();
    }
}